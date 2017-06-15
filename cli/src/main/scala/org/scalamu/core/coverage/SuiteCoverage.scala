package org.scalamu.core.coverage

import org.scalamu.core.workers.MeasuredSuite

final case class StatementId(id: Int) extends AnyVal

final case class SuiteCoverage(suite: MeasuredSuite, coverage: Set[StatementId])
