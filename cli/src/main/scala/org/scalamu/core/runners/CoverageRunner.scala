package org.scalamu.core
package runners

import java.io.DataInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

import cats.data.ValidatedNel
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.core.coverage._
import org.scalamu.core.process._
import org.scalamu.testapi.{CompositeFramework, SuiteFailure, TestClassFileFinder}

object CoverageRunner extends Runner[ValidatedNel[SuiteFailure, SuiteCoverage]] {
  private val log = Logger[CoverageRunner.type]

  override type Configuration = (CoverageRunnerConfig, Path)

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    for {
      config    <- decode[CoverageRunnerConfig](dis.readUTF())
      outputDir <- decode[Path](dis.readUTF())
    } yield (config, outputDir)

  override def run(
    configuration: Configuration
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

  def main(args: Array[String]): Unit = execute(args)
}
