package org.scalamu.core.coverage

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.instances.list._
import cats.syntax.traverse._
import org.scalamu.testapi.{AbstractTestSuite, SuiteFailure, SuiteSuccess}

class StatementCoverageAnalyzer(
  reader: InvocationDataReader,
  reporter: InstrumentationReporter
) {
  def forSuites(
    suites: List[AbstractTestSuite]
  ): ValidatedNel[SuiteFailure, List[SuiteCoverage]] =
    suites.traverseU(forSuite)

  def forSuite(suite: AbstractTestSuite): ValidatedNel[SuiteFailure, SuiteCoverage] =
    suite.execute() match {
      case _: SuiteSuccess =>
        val statements = reader.invokedStatements().map(reporter.getStatementById)
        valid(SuiteCoverage(suite, statements)).toValidatedNel
      case sf: SuiteFailure =>
        reader.clearData()
        invalidNel(sf)
    }
}
