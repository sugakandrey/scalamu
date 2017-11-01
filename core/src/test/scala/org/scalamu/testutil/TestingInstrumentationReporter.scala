package org.scalamu.testutil

import org.scalamu.core.coverage.{MemoryReporter, Statement, StatementId}

import scala.collection.JavaConverters._

class TestingInstrumentationReporter extends MemoryReporter {
  def statements(): Map[StatementId, Statement] = instrumentedStatements.asScala.toMap
}
