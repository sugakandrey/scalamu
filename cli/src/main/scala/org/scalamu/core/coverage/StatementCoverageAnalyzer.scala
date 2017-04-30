package org.scalamu.core.coverage

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.instances.list._
import cats.syntax.traverse._
import org.scalamu.testapi.{AbstractTestSuite, SuiteFailure}

import scala.collection.Set

/**
 *
 * @param reader
 * @param reporter
 */
class StatementCoverageAnalyzer(
  reader: InvocationDataReader,
  reporter: InstrumentationReporter
) {
  type SuiteCoverage = (AbstractTestSuite, Set[Statement])

  def forSuites(
    suites: List[AbstractTestSuite]
  ): ValidatedNel[SuiteFailure, Map[AbstractTestSuite, Set[Statement]]] =
    suites.traverseU(suiteCoverage).map(_.toMap)

  private def suiteCoverage(suite: AbstractTestSuite): ValidatedNel[SuiteFailure, SuiteCoverage] = {
    import org.scalamu.testapi.TestSuiteResult._

    suite.execute() match {
      case _: Success =>
        val statements = reader.invokedStatements().map(reporter.getStatementById)
        valid(suite -> statements).toValidatedNel
      case sf: SuiteFailure => reader.clearData(); invalidNel(sf)
    }
  }
}
