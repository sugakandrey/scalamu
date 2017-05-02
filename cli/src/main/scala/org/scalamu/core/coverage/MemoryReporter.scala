package org.scalamu.core.coverage

import java.{util => ju}

import org.scalamu.common.position.Position
import scoverage.Coverage

class MemoryReporter extends InstrumentationReporter {
  protected val instrumentedStatements: ju.Map[Int, Statement] = new ju.HashMap[Int, Statement]()

  def onInstrumentationFinished(coverage: Coverage): Unit =
    coverage.statements.foreach(
      stm =>
        instrumentedStatements.put(
          stm.id,
          Statement(
            stm.id,
            stm.line,
            Position(stm.source, stm.start, stm.end)
          )
      )
    )

  override def getStatementById(id: Int): Statement = instrumentedStatements.get(id)
}
