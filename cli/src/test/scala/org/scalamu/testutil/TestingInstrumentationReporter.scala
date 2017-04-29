package org.scalamu.testutil

import org.scalamu.core.coverage.{MemoryReporter, Statement}

import scala.collection.JavaConverters._

class TestingInstrumentationReporter extends MemoryReporter {
  def statements(): Map[Int, Statement] = instrumentedStatements.asScala.toMap
}
