package org.scalamu.testapi

trait TestingFramework {
  def name: String
  def runner: TestRunner
  def filter: TestClassFilter
}
