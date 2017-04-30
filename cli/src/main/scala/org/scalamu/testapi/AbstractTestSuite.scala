package org.scalamu.testapi

import org.scalamu.core.ClassInfo

trait AbstractTestSuite {
  def info: ClassInfo
  def execute(): TestSuiteResult
}
