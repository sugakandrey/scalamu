package org.scalamu.core.coverage

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.instances.list._
import cats.syntax.traverse._
import org.scalamu.testapi.{AbstractTestSuite, SuiteFailure, SuiteSuccess}

class StatementCoverageAnalyzer(
  reader: InvocationDataReader
) {
  def forSuites(
    suites: List[AbstractTestSuite]
  ): ValidatedNel[SuiteFailure, List[SuiteCoverage]] =
      suites.traverseU(forSuite)

  def forSuite(suite: AbstractTestSuite): ValidatedNel[SuiteFailure, SuiteCoverage] =
    suite.execute() match {
      case _: SuiteSuccess =>
        val statements = reader.invokedStatements().map(StatementId(_))
        valid(SuiteCoverage(suite, statements))
      case sf: SuiteFailure =>
        reader.clearData()
        invalidNel(sf)
    }
}
