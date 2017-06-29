package org.scalamu.core
package runners

import java.net.ServerSocket
import java.nio.file.Path

import cats.instances.list._
import cats.instances.either._
import cats.syntax.either._
import cats.syntax.traverse._
import com.typesafe.scalalogging.Logger
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.{InstrumentationReporter, Statement, StatementId, SuiteCoverage}
import org.scalamu.core.process.MeasuredSuite

import scala.collection.breakOut
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{blocking, Await, Future}
import scala.concurrent.duration._

class CoverageAnalyser(
  val config: ScalamuConfig,
  val compiledSourcesDir: Path
) {
  import CoverageAnalyser._
  private type Result = CoverageRunner#Result
  
  private def reportAndExit(message: String): Nothing = {
    log.error(message)
    die(InternalFailure)
  }

  private def receiveCoverageData(
    supervisor: ProcessSupervisor[Nothing, Result]
  ): Either[FailingSuites, List[SuiteCoverage]] = {
    val coverageStart = System.currentTimeMillis()
    val pipe          = supervisor.communicationPipe

    val workerData: Either[CommunicationException, List[Result]] =
      Iterator.continually(pipe.receive()).takeWhile(_.isRight).toList.sequenceU

    val coverageData = workerData match {
      case Left(communicationException) =>
        log.error(
          s"Timed out while waiting for coverage report: $communicationException. " +
            s"Make sure the environment is correctly set up."
        )
        die(InternalFailure)
      case Right(result) => result.sequenceU.toEither.leftMap(FailingSuites)
    }

    val coverageDuration = (System.currentTimeMillis() - coverageStart) / 1000
    log.info(
      s"Coverage process finished in $coverageDuration seconds with exit code ${supervisor.waitFor()}."
    )

    coverageData
  }

  def analyse(instrumentation: InstrumentationReporter): Map[MeasuredSuite, Set[Statement]] = {
    val socket = new ServerSocket(0)
    val runner = new CoverageRunner(socket, config, compiledSourcesDir)

    val supervisor = runner
      .start()
      .valueOr(error => reportAndExit(s"Failed to launch coverage process. Cause: $error."))

    val coverageFuture = Future { blocking { receiveCoverageData(supervisor) } }
    val timeLimit      = 1 minute
    val coverageData   = Either.catchNonFatal(Await.result(coverageFuture, timeLimit))

    val coverage: Map[MeasuredSuite, Set[StatementId]] = coverageData match {
      case Left(timeout) =>
        reportAndExit(
          s"Timed out while waiting for coverage report. Time limit: $timeLimit. $timeout. " +
            s"Make sure the environment is correctly set up."
        )
      case Right(failingSuitesOrCoverage) =>
        failingSuitesOrCoverage match {
          case Left(failingSuites) =>
            reportAndExit(
              s"Mutation analysis requires green test suite, " +
                s"but the following tests failed: $failingSuites"
            )
          case Right(suitesCoverage) =>
            suitesCoverage.map(cov => cov.suite -> cov.coverage)(breakOut)
        }
    }

    coverage.mapValues(_.map(instrumentation.getStatementById))
  }
}

object CoverageAnalyser {
  private val log = Logger[CoverageAnalyser]
}
