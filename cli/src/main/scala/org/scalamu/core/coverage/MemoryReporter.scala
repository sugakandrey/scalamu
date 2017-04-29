package org.scalamu.core.coverage

import java.{util => ju}

import scoverage.Coverage

class MemoryReporter extends InstrumentationReporter {
  protected val instrumentedStatements: ju.Map[Int, Statement] = new ju.HashMap[Int, Statement]()

  def onInstrumentationFinished(coverage: Coverage): Unit =
    coverage.statements.foreach(
      stm =>
        instrumentedStatements.put(
          stm.id,
          Statement(
            stm.source,
            stm.line,
            Position(stm.start, stm.end)
          )
      )
    )

  override def getStatementById(id: Int): Statement = instrumentedStatements.get(id)
}
