package org.scalamu.core
package runners

import java.nio.file.Paths

import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage._
import org.scalamu.testapi.{AbstractTestSuite, CompositeFramework, TestClassFileFinder}
import scoverage.IOUtils

import scala.collection.Set

object CoverageRunner {
  def main(args: Array[String]): Unit = run(???)

  def run(config: ScalamuConfig): Either[FailingSuites, Map[AbstractTestSuite, Set[Statement]]] = {
    val reader   = new InvocationDataReader(Paths.get(IOUtils.getTempPath))
    val reporter = new MemoryReporter
    val analyzer = new StatementCoverageAnalyzer(reader, reporter)
    val suites   = new TestClassFileFinder(CompositeFramework.filter).findAll(config.testClassDirs)
    val coverage = analyzer.forSuites(suites)
    coverage.toEither.left.map(FailingSuites)
  }
}
