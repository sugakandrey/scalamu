package org.scalamu.core
package workers

import java.io.DataInputStream
import java.nio.file.Path

import cats.data.ValidatedNel
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.core.coverage._
import org.scalamu.core.runners._
import org.scalamu.testapi.{CompositeFramework, SuiteFailure, TestClassFileFinder}

object CoverageWorker extends Worker[ValidatedNel[SuiteFailure, SuiteCoverage]] {
  private val log = Logger[CoverageWorker.type]

  override type Configuration = (CoverageWorkerConfig, Path)

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    for {
      config    <- decode[CoverageWorkerConfig](dis.readUTF())
      outputDir <- decode[Path](dis.readUTF())
    } yield (config, outputDir)

  override def run(
    configuration: Configuration,
    dis: DataInputStream
  ): Iterator[Result] = {
    val (config, invocationDataDir) = configuration
    val reader                      = new InvocationDataReader(invocationDataDir)
    reader.clearData()
    log.debug(s"Initialized InvocationDataReader in $invocationDataDir.")
    val analyzer = new StatementCoverageAnalyzer(reader)
    val suites = new TestClassFileFinder(
      new CompositeFramework(config.excludeTestsClasses: _*).filter
    ).findAll(config.testClassDirs)
    log.info(s"Discovered ${suites.size} test suites. Analyzing coverage now...")
    suites.iterator.map(analyzer.forSuite)
  }

  def main(args: Array[String]): Unit = {
    LoggerConfiguration.configurePatternForName("COVERAGE-WORKER")
    execute(args)
  }
}
