package org.scalamu.core.coverage

import scoverage.Coverage

trait InstrumentationReporter {
  def onInstrumentationFinished(coverage: Coverage): Unit
  def getStatementById(id: StatementId): Statement
}
