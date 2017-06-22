package org.scalamu.core.runners

import java.net.ServerSocket
import java.nio.file.Path

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.MeasuredSuite
import org.scalamu.core._
import org.scalamu.plugin.MutantInfo

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MutationAnalyser(
  socket: ServerSocket,
  val config: ScalamuConfig,
  val compiledSourcesDir: Path
) {

  import MutationAnalyser._

  def analyse(
    coverage: Map[MutantId, Set[MeasuredSuite]],
    mutationsById: Map[MutantId, MutantInfo]
  ): Set[TestedMutant] = {
    val analysed = runMutationAnalysis(coverage, mutationsById)
    val untested = analysed.collect { case tm if tm.status == Untested => tm.info.id }

    val rerun: Set[TestedMutant] =
      if (untested.isEmpty) Set.empty
      else {
        val remainingCoverage = coverage.filterKeys(untested.contains)
        val remainingIds      = mutationsById.filterKeys(untested.contains)
        analyse(remainingCoverage, remainingIds)
      }

    analysed.filterNot(_.status == Untested) | rerun
  }

  def runMutationAnalysis(
    coverage: Map[MutantId, Set[MeasuredSuite]],
    mutationsById: Map[MutantId, MutantInfo]
  ): Set[TestedMutant] = {
    val runner = new MutationAnalysisRunner(socket, config, compiledSourcesDir, coverage)
    log.debug(s"Created new MutationAnalysisRunner. Mutations to analyse: ${coverage.size}")
    val result = runner.execute()

    val timeLimiter: (Long) => Long =
      workers.testTimeLimit(_, config.timeoutFactor, config.timeoutConst)

    val testsTimeLimit =
      coverage.values.map(_.map(t => timeLimiter(t.completionTimeMillis)).sum).sum millis

    val runnerOverhead: Duration = 1 minute
    val totalTimeLimit           = runnerOverhead + testsTimeLimit

    val failureHandler = (failure: ScalamuFailure) => {
      log.error(
        s"An internal failure occurred when running mutation analysis " +
          s"(cause: $failure), the process will now exit."
      )
      coverage.keysIterator.map(id => TestedMutant(mutationsById(id), InternalFailure)).toSet
    }

    Either.catchNonFatal(Await.result(result, totalTimeLimit)) match {
      case Left(failure) => failureHandler(CommunicationException(failure))
      case Right(runnerExecutionResult) =>
        runnerExecutionResult match {
          case Left(failure) => failureHandler(failure)
          case Right(results) =>
            results.map(r => TestedMutant(mutationsById(r.id), r.status))(collection.breakOut)
        }
    }
  }
}

object MutationAnalyser {
  private val log = Logger[MutationAnalyser]
}
