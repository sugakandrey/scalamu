package org.scalamu.core
package runners

import java.net.ServerSocket
import java.nio.file.Path

import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.typesafe.scalalogging.Logger
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.{InstrumentationReporter, Statement, StatementId, SuiteCoverage}
import org.scalamu.core.workers.{ExitCode, MeasuredSuite}

import scala.collection.breakOut
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class CoverageAnalyser(
  val config: ScalamuConfig,
  val compiledSourcesDir: Path
) {
  import CoverageAnalyser._

  def analyse(instrumentation: InstrumentationReporter): Map[MeasuredSuite, Set[Statement]] = {
    val socket          = new ServerSocket(0)
    val coverageProcess = new CoverageRunner(socket, config, compiledSourcesDir)
    val coverageFuture  = coverageProcess.execute()
    val coverageStart   = System.currentTimeMillis()
    val coverageResults = Either.catchNonFatal(Await.result(coverageFuture, 1 minutes))

    val coverageDuration = (System.currentTimeMillis() - coverageStart) / 1000
    log.info(s"Finished analysing coverage in $coverageDuration seconds.")

    val reportAndExit = exit(log.error(_))(_: String, ExitCode.RuntimeFailure)

    val coverageData: Either[FailingSuites, List[SuiteCoverage]] = coverageResults match {
      case Left(timeout) =>
        reportAndExit(
          s"Timed out while waiting for coverage report: $timeout. " +
            s"Make sure the environment is correctly set up."
        )
      case Right(result) =>
        result match {
          case Right(runnerResults) => runnerResults.sequenceU.toEither.leftMap(FailingSuites)
          case Left(err) =>
            reportAndExit(s"An error occurred while communicating with CoverageRunner. $err")
        }
    }

    log.info(
      s"Coverage process finished in $coverageDuration seconds with exit code ${coverageProcess.exitValue()}."
    )

    val coverage: Map[MeasuredSuite, Set[StatementId]] = coverageData match {
      case Left(failingSuites) =>
        reportAndExit(
          s"Mutation analysis requires green suite but the following suites failed: $failingSuites."
        )
      case Right(suiteCoverages) => suiteCoverages.map(cov => cov.suite -> cov.coverage)(breakOut)
    }

    coverage.mapValues(_.map(instrumentation.getStatementById))
  }
}

object CoverageAnalyser {
  private val log = Logger[CoverageAnalyser]
}
