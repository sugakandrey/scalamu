package org.scalamu.testapi

import org.scalamu.core.ClassFileInfo

final case class TestClassFileInfo(
  info: ClassFileInfo,
  testingFramework: TestingFramework
)
