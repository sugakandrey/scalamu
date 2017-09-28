package org.scalamu.core
package process

import java.io.{DataInputStream, DataOutputStream}
import java.nio.file.Path

import cats.data.ValidatedNel
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.core.coverage._
import org.scalamu.core.runners._
import org.scalamu.testapi.{SuiteFailure, TestClassFileFinder, TestClassFilter, TestingFramework}

object CoverageProcess extends Process[ValidatedNel[SuiteFailure, SuiteCoverage]] {
  private val log = Logger[CoverageProcess.type]

  override type Configuration = (CoverageProcessConfig, Path)

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    for {
      config    <- decode[CoverageProcessConfig](dis.readUTF()).right
      outputDir <- decode[Path](dis.readUTF()).right
    } yield (config, outputDir)

  override def run(
    configuration: Configuration,
    dis: DataInputStream,
    dos: DataOutputStream
  ): Iterator[Result] = {
    val (config, invocationDataDir) = configuration
    val reader                      = new InvocationDataReader(invocationDataDir)

    reader.clearData()
    log.debug(s"Initialized InvocationDataReader in $invocationDataDir.")
    val analyzer   = new StatementCoverageAnalyzer(reader)
    val frameworks = TestingFramework.instantiateAvailableFrameworks(config.testingOptions)
    log.debug(
      s"Searching for tests conforming to the following frameworks: ${frameworks.map(_.name).mkString(", ")}."
    )

    val filter = TestClassFilter.forFrameworks(frameworks, config.targetTests)
    val finder = new TestClassFileFinder(filter)
    val suites = finder.findAll(config.testClassDirs)
    log.info(s"Discovered ${suites.size} test suites. Analyzing coverage now...")
    suites.iterator.map(analyzer.forSuite)
  }

  def main(args: Array[String]): Unit = {
    LoggerConfiguration.configureLoggingForName("COVERAGE-WORKER")
    execute(args)
  }
}
