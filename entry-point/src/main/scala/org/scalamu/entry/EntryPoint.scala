package org.scalamu.entry

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.compilation.ScalamuGlobal
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.{InverseMutantCoverage, Statement}
import org.scalamu.core.detection.SourceFileFinder
import org.scalamu.core.runners._
import org.scalamu.core.workers.ExitCode
import org.scalamu.core.{LoggerConfiguration, SourceInfo, TestedMutant, coverage => cov}
import org.scalamu.plugin.MutantInfo
import org.scalamu.report.{HtmlReportWriter, ProjectSummaryFactory}
import org.scalamu.testapi.AbstractTestSuite
import org.scalamu.utils.FileSystemUtils._
import org.scalamu.{core, plugin}

import scala.collection.JavaConverters._
import scala.collection.breakOut
import scala.reflect.io.{Directory, PlainDirectory}

object EntryPoint {
  private val log  = Logger[EntryPoint.type]
  private val exit = core.exit(log.error(_))(_: String, ExitCode.RuntimeFailure)

  private def ensureDirExits(dir: Path): Unit =
    if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    }

  def main(args: Array[String]): Unit = {
    LoggerConfiguration.configurePatternForName("MAIN-APP")
    val config = ScalamuConfig.parseConfig(args)
    if (config.verbose) {
      log.info(s"Running Scalamu with config:\n ${config.asJson.spaces2}")
    }

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

    val coverageAnalyser = new CoverageAnalyser(config, outputPath)
    val coverage         = coverageAnalyser.analyse(instrumentation)

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
      coverage,
      reporter.mutants
    )

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "inverseCoverage.log")) { writer =>
        inverseCoverage.foreach(mCov => writer.write(mCov.asJson.spaces2 + "\n"))
      }
    }

    val mutantsById: Map[MutantId, MutantInfo] = reporter.mutants.map(m => m.id -> m)(breakOut)

    val analyser      = new MutationAnalyser(config, outputPath)
    val testedMutants = analyser.analyse(inverseCoverage, mutantsById)

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "results.log")) { writer =>
        testedMutants.foreach(mutant => writer.write(mutant.asJson.spaces2 + "\n"))
      }
    }

    val invoked: Set[Statement] = coverage.valuesIterator.flatten.toSet

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
