package org.scalamu.core.coverage

import org.scalamu.testapi.AbstractTestSuite

final case class StatementId(id: Int) extends AnyVal

final case class SuiteCoverage(suite: AbstractTestSuite, coverage: Set[StatementId])
