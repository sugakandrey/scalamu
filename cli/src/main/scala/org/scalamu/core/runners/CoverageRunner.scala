package org.scalamu.core
package runners

import java.nio.file.Paths

import com.typesafe.scalalogging.Logger
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage._
import org.scalamu.testapi.{AbstractTestSuite, CompositeFramework, TestClassFileFinder}
import scoverage.IOUtils

import scala.collection.Set

object CoverageRunner {
  private val log = Logger[CoverageRunner.type]

  def main(args: Array[String]): Unit = run(???, ???)

  def run(
    config: ScalamuConfig,
    reporter: InstrumentationReporter
  ): Either[FailingSuites, Map[AbstractTestSuite, Set[Statement]]] = {
    val reader   = new InvocationDataReader(Paths.get(IOUtils.getTempPath))
    val analyzer = new StatementCoverageAnalyzer(reader, reporter)
    val suites = new TestClassFileFinder(
      new CompositeFramework(config.excludeTestsClasses: _*).filter
    ).findAll(config.testClassDirs)
    log.debug(s"Suites discovered: ${suites.map(_.info.name.fullName)}. Analyzing coverage now...")
    val coverage = analyzer.forSuites(suites)
    log.debug(s"Finished statement coverage analysis.")
    coverage.toEither.left.map(FailingSuites)
  }
}
