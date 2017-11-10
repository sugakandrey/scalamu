package org.scalamu.core.coverage

import java.{util => ju}

import org.scalamu.common.position.Position
import scoverage.Coverage

class MemoryReporter(
  val instrumentedStatements: ju.Map[StatementId, Statement] = new ju.HashMap[StatementId, Statement]
) extends InstrumentationReporter {
  def onInstrumentationFinished(coverage: Coverage): Unit = {
    scribe.info(s"Finished coverage instrumentation. Total ${coverage.statements.size} statements.")
    coverage.statements.foreach(
      stm =>
        instrumentedStatements.put(
          StatementId(stm.id),
          Statement(
            StatementId(stm.id),
            stm.location,
            Position(stm.source, stm.line, stm.start, stm.end)
          )
      )
    )
  }

  override def getStatementById(id: StatementId): Statement = instrumentedStatements.get(id)
}
