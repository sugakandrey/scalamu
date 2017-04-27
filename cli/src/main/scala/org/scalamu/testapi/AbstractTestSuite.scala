package org.scalamu.testapi

trait AbstractTestSuite {
  def execute(): TestSuiteResult
}
