package org.scalamu.testapi

import org.scalamu.core.ClassInfo

/**
 * Represents a single test class.
 *
 * @param info             [[org.scalamu.core.ClassInfo]] instance containing information about this class
 * @param testingFramework to which this test class belongs.
 */
final case class TestClassInfo(
  info: ClassInfo,
  testingFramework: TestingFramework
)
