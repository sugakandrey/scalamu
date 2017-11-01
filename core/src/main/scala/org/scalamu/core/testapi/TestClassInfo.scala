package org.scalamu.core.testapi

import org.scalamu.core.api.ClassInfo

/**
 * Represents a single test class.
 *
 * @param info             [[org.scalamu.core.api.ClassInfo]] instance containing information about this class
 * @param testingFramework to which this test class belongs
 */
final case class TestClassInfo(
  override val info: ClassInfo,
  testingFramework: TestingFramework
) extends AbstractTestSuite {
  override def execute(): TestSuiteResult = testingFramework.runner.run(info.name)
  override def toString: String           = s"${testingFramework.name} test:".formatted("%-15s") + s" ${info.name.fullName}"
}
