package org.scalamu.core
package runners

import java.io.DataInputStream
import java.nio.file.Paths

import cats.data.ValidatedNel
import com.typesafe.scalalogging.Logger
import org.scalamu.core.process._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.core.coverage._
import org.scalamu.core.process.CoverageRunnerConfig
import org.scalamu.testapi.{CompositeFramework, SuiteFailure, TestClassFileFinder}
import scoverage.IOUtils

object CoverageRunner extends Runner[ValidatedNel[SuiteFailure, SuiteCoverage]] {
  private val log = Logger[CoverageRunner.type]

  override type Configuration = (CoverageRunnerConfig, InstrumentationReporter)

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, (CoverageRunnerConfig, InstrumentationReporter)] =
    for {
      config   <- decode[CoverageRunnerConfig](dis.readUTF())
      reporter <- decode[InstrumentationReporter](dis.readUTF())
    } yield (config, reporter)

  override def run(
    configuration: Configuration
  ): Iterator[Result] = {
    val (config, reporter) = configuration
    val reader             = new InvocationDataReader(Paths.get(IOUtils.getTempPath))
    val analyzer           = new StatementCoverageAnalyzer(reader, reporter)
    val suites = new TestClassFileFinder(
      new CompositeFramework(config.excludeTestsClasses: _*).filter
    ).findAll(config.testClassDirs)
    log.debug(s"Suites discovered: ${suites.map(_.info.name.fullName)}. Analyzing coverage now...")
    suites.iterator.map(analyzer.forSuite)
  }

  def main(args: Array[String]): Unit = execute(args)
}
