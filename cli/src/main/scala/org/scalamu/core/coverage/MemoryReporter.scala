package org.scalamu.core.coverage

import java.{util => ju}

import com.typesafe.scalalogging.Logger
import org.scalamu.common.position.Position
import scoverage.Coverage

class MemoryReporter(
  val instrumentedStatements: ju.Map[StatementId, Statement] = new ju.HashMap[StatementId, Statement]
) extends InstrumentationReporter {
  import MemoryReporter._

  def onInstrumentationFinished(coverage: Coverage): Unit = {
    log.info(s"Finished coverage instrumentation. Total ${coverage.statements.size} statements.")
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

object MemoryReporter {
  private val log = Logger[MemoryReporter]
}
