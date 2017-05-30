package org.scalamu.entry

import java.io.{BufferedWriter, FileWriter}
import java.net.ServerSocket
import java.nio.file.{Files, Path, Paths}

import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.compilation.ScalamuGlobal
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.{InverseMutantCoverage, Statement, StatementId, SuiteCoverage}
import org.scalamu.core.detection.SourceFileFinder
import org.scalamu.core.process._
import org.scalamu.core.runners.MutationRunnerResponse
import org.scalamu.core.{FailingSuites, SourceInfo, TestedMutant, coverage => cov}
import org.scalamu.plugin
import org.scalamu.utils.FileSystemUtils._
import org.scalamu.plugin.MutantInfo
import org.scalamu.report.{HtmlReportWriter, ProjectSummaryFactory}
import org.scalamu.testapi.AbstractTestSuite

import scala.collection.JavaConverters._
import scala.collection.breakOut
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.duration._
import scala.reflect.io.{Directory, PlainDirectory}

object EntryPoint {
  private val log = Logger[EntryPoint.type]

  private def exit(errorMsg: String): Nothing = {
    log.error(errorMsg)
    sys.exit(1)
  }

  private def ensureDirExits(dir: Path): Unit =
    if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    }

  def main(args: Array[String]): Unit = {
    val config    = ScalamuConfig.parseConfig(args)
    val reportDir = Paths.get("mutation-analysis-report")
    ensureDirExits(reportDir)

    val instrumentation = new cov.MemoryReporter
    val reporter        = new plugin.MemoryReporter

    val outputPath = Files.createTempDirectory("mutated-classes")
    val outDir     = new PlainDirectory(new Directory(outputPath.toFile))

    val global      = ScalamuGlobal(config, instrumentation, reporter, outDir)
    val sourceFiles = new SourceFileFinder().findAll(config.sourceDirs)

    if (config.verbose) {
      tryWith(new BufferedWriter(new FileWriter(s"$reportDir/sourceFiles.log"))) { writer =>
        sourceFiles.foreach(sf => writer.write(sf.asJson.spaces2 + "\n"))
      }
    }
    val compilationStart = System.currentTimeMillis()
    global.compile(sourceFiles)
    val compilationTime = (System.currentTimeMillis() - compilationStart) / 1000
    log.info(s"Finished recompilation in $compilationTime seconds.")
    log.info(s"Total mutations generated: ${reporter.mutants.size}")

    if (reporter.mutants.isEmpty) {
      exit("No mutants were generated. Make sure you have correctly applied exclusion filters.")
    }

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "mutations.log")) { writer =>
        reporter.mutants.foreach(m => writer.write(m.asJson.spaces2 + "\n"))
      }
    }

    val socket          = new ServerSocket(0)
    val coverageProcess = new CoverageProcess(socket, config, outputPath)
    val coverageFuture  = coverageProcess.execute()
    val coverageStart   = System.currentTimeMillis()
    val coverageResults = Either.catchNonFatal(Await.result(coverageFuture, 1 minutes))

    val coverageData: Either[FailingSuites, List[SuiteCoverage]] = coverageResults match {
      case Left(timeout) =>
        exit(
          s"Timed out while waiting for coverage report: $timeout. " +
            s"Make sure the environment is correctly set up."
        )
      case Right(result) =>
        result match {
          case Right(runnerResults) => runnerResults.sequenceU.toEither.leftMap(FailingSuites)
          case Left(err) =>
            exit(s"An error occurred while communicating with CoverageRunner. $err")
        }
    }

    val coverageDuration = (System.currentTimeMillis() - coverageStart) / 1000
    log.info(
      s"Coverage process finished in $coverageDuration seconds with exit code ${coverageProcess.exitCode()}."
    )

    val coverage: Map[AbstractTestSuite, Set[StatementId]] = coverageData match {
      case Left(failingSuites) =>
        exit(
          s"Mutation analysis requires green suite but the following suites failed: $failingSuites."
        )
      case Right(suiteCoverages) => suiteCoverages.map(cov => cov.suite -> cov.coverage)(breakOut)
    }

    log.debug(s"Test suites examined: ${coverage.keySet.mkString("[\n\t", "\n\t", "\n]")}.")

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "testSuites.log")) { writer =>
        coverage.foreach(suite => writer.write(suite.asJson.spaces2 + "\n"))
      }
      tryWith(Files.newBufferedWriter(reportDir / "statements.log")) { writer =>
        instrumentation.instrumentedStatements
          .values()
          .forEach(v => writer.write(v.asJson.spaces2 + "\n"))
      }
    }

    val inverseCoverage = InverseMutantCoverage.fromStatementCoverage(
      coverage.mapValues(_.map(instrumentation.getStatementById)),
      reporter.mutants
    )

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "inverseCoverage.log")) { writer =>
        inverseCoverage.foreach(mCov => writer.write(mCov.asJson.spaces2 + "\n"))
      }
    }

    val analysisProcess = new MutationAnalysisProcess(socket, config, outputPath, inverseCoverage)
    val analysisFuture  = analysisProcess.execute()
    val analysisResults = Either.catchNonFatal(Await.result(analysisFuture, 2 minutes))

    val analysisData: Seq[MutationRunnerResponse] = analysisResults match {
      case Left(timeout) =>
        exit(
          s"Timed out while waiting for mutation analysis report: $timeout. " +
            s"Make sure the environment is correctly set up."
        )
      case Right(result) =>
        result match {
          case Right(runnerResults) => runnerResults
          case Left(err) =>
            exit(s"An error occurred while communicating with MutationAnalysisRunner. $err")
        }
    }

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "results.log")) { writer =>
        analysisData.foreach(mutant => writer.write(mutant.asJson.spaces2 + "\n"))
      }
    }

    val mutantsById: Map[MutantId, MutantInfo] = reporter.mutants.map(m => m.id -> m)(breakOut)

    val testedMutants: Set[TestedMutant] =
      analysisData.map(response => TestedMutant(mutantsById(response.id), response.status))(
        breakOut
      )

    val invoked: Set[Statement] =
      coverage.values.flatMap(ids => ids.map(instrumentation.getStatementById))(breakOut)

    generateReport(
      reportDir,
      sourceFiles.toSet,
      testedMutants,
      instrumentation.instrumentedStatements.values().asScala.toSet,
      invoked,
      Set.empty,
      config
    )
  }

  def generateReport(
    targetDir: Path,
    sourceFiles: Set[SourceInfo],
    testedMutants: Set[TestedMutant],
    statements: Set[Statement],
    invokedStatements: Set[Statement],
    tests: Set[AbstractTestSuite],
    config: ScalamuConfig
  ): Unit = {
    val summary = ProjectSummaryFactory(
      statements,
      invokedStatements,
      testedMutants,
      sourceFiles,
      tests
    )
    HtmlReportWriter.generateFromProjectSummary(summary, config, targetDir)
  }
}
