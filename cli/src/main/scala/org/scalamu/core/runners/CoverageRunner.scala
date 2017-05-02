package org.scalamu.core
package runners

import java.nio.file.Paths

import cats.data.ValidatedNel
import com.typesafe.scalalogging.Logger
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage._
import org.scalamu.testapi.{CompositeFramework, SuiteFailure, TestClassFileFinder}
import scoverage.IOUtils

object CoverageRunner {
  private val log = Logger[CoverageRunner.type]

  type ProcessData = ValidatedNel[SuiteFailure, SuiteCoverage]

  def main(args: Array[String]): Unit = {
    val port = args
    run(???, ???)
  }

  def run(
    config: ScalamuConfig,
    reporter: InstrumentationReporter
  ): Iterator[ProcessData] = {
    val reader   = new InvocationDataReader(Paths.get(IOUtils.getTempPath))
    val analyzer = new StatementCoverageAnalyzer(reader, reporter)
    val suites = new TestClassFileFinder(
      new CompositeFramework(config.excludeTestsClasses: _*).filter
    ).findAll(config.testClassDirs)
    log.debug(s"Suites discovered: ${suites.map(_.info.name.fullName)}. Analyzing coverage now...")
    suites.iterator.map(analyzer.forSuite)
  }
}
