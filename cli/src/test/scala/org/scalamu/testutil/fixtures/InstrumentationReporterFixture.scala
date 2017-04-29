package org.scalamu.testutil.fixtures

import org.scalamu.testutil.TestingInstrumentationReporter
import org.scalatest.{BeforeAndAfterAll, TestSuite}

trait InstrumentationReporterFixture extends TestSuite {
  def instrumentationReporter: TestingInstrumentationReporter

  def withInstrumentationReporter(code: TestingInstrumentationReporter => Any): Any
}

trait SharedInstrumentationReporterFixture
    extends InstrumentationReporterFixture
    with BeforeAndAfterAll {
  private[scalamu] var instrumentation: TestingInstrumentationReporter = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    instrumentation = instrumentationReporter
  }

  override def withInstrumentationReporter(
    code: TestingInstrumentationReporter => Any
  ): Any = code(instrumentation)
}

trait IsolatedInstrumentationReporterFixture extends InstrumentationReporterFixture {
  override def withInstrumentationReporter(code: (TestingInstrumentationReporter) => Any): Any =
    code(instrumentationReporter)
}
