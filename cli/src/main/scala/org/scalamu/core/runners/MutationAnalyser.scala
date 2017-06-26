package org.scalamu.core.runners

import java.net.ServerSocket
import java.nio.file.Path

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.core._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.MeasuredSuite
import org.scalamu.plugin.MutantInfo

import scala.collection.breakOut
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, blocking}

class MutationAnalyser(
  val config: ScalamuConfig,
  val compiledSourcesDir: Path
) {
  import MutationAnalyser._

  private def calculateTimeLimit(coverage: Map[MutantId, Set[MeasuredSuite]]): Duration = {
    val testTimeLimiter: (Long) => Long =
      workers.testTimeLimit(_, config.timeoutFactor, config.timeoutConst)

    val totalTestsDuration =
      coverage.values.map(_.map(t => testTimeLimiter(t.completionTimeMillis)).sum).sum millis

    val approximateRestarts    = Math.ceil(coverage.size * 1d / 50)
    val runnerCreationOverhead = 20 seconds
    val runnerOverhead         = approximateRestarts * runnerCreationOverhead
    runnerOverhead + totalTestsDuration
  }

  private def failureHandler(
    failure: ScalamuFailure,
    workerId: Long,
    coverage: TraversableOnce[MutantId],
    mutationsById: MutantId => MutantInfo
  ): Set[TestedMutant] = {
    log.error(
      s"An internal failure occurred when running mutation analysis workerId: $workerId" +
        s" (cause: $failure), the process will now exit."
    )
    coverage.map(id => TestedMutant(mutationsById(id), InternalFailure)).toSet
  }

  def analyse(
    coverage: Map[MutantId, Set[MeasuredSuite]],
    mutationsById: Map[MutantId, MutantInfo]
  ): Set[TestedMutant] = {
    val chunkSize = Math.ceil(coverage.size * 1d / config.parallelism).toInt
    val chunks    = coverage.grouped(chunkSize)

    def analyseAux(
      coverageChunk: Map[MutantId, Set[MeasuredSuite]]
    ): Set[TestedMutant] = {
      val analysed = runMutationAnalysis(coverageChunk, mutationsById)
      val untested = analysed.collect { case tm if tm.status == Untested => tm.info.id }

      val rerun: Set[TestedMutant] =
        if (untested.isEmpty) Set.empty
        else {
          val remainingCoverage = coverageChunk.filterKeys(untested.contains)
          analyseAux(remainingCoverage)
        }

      analysed.filterNot(_.status == Untested) | rerun
    }

    val resultsFuture = Future.traverse(chunks)(chunk => Future { blocking { analyseAux(chunk) } })

    val allResults =
      Either.catchNonFatal(Await.result(resultsFuture, calculateTimeLimit(coverage)))

    allResults match {
      case Left(err) =>
        failureHandler(
          CommunicationException(err),
          -1,
          coverage.keysIterator,
          mutationsById
        )
      case Right(results) => results.foldLeft(Set.empty[TestedMutant])(_ | _)
    }
  }

  def runMutationAnalysis(
    coverage: Map[MutantId, Set[MeasuredSuite]],
    mutationsById: Map[MutantId, MutantInfo]
  ): Set[TestedMutant] = {
    val workerId = Thread.currentThread().getId
    val handler  = failureHandler(_: ScalamuFailure, workerId, coverage.keysIterator, mutationsById)

    val openSocket = Either.catchNonFatal(new ServerSocket(0))

    openSocket match {
      case Left(exception) => handler(CommunicationException(exception))
      case Right(socket) =>
        val runner = new MutationAnalysisRunner(socket, config, compiledSourcesDir, coverage, workerId)

        log.debug(
          s"Created new MutationAnalysisRunner id: $workerId. Mutations to analyse: ${coverage.size}"
        )

        val result    = runner.execute()
        val timeLimit = calculateTimeLimit(coverage)

        Either.catchNonFatal(Await.result(result, timeLimit)) match {
          case Left(failure) => handler(CommunicationException(failure))
          case Right(runnerExecutionResult) =>
            runnerExecutionResult match {
              case Left(failure) => handler(failure)
              case Right(results) =>
                results.map(r => TestedMutant(mutationsById(r.id), r.status))(breakOut)
            }
        }
    }
  }
}

object MutationAnalyser {
  private val log = Logger[MutationAnalyser]
}
