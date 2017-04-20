package org.scalamu.testapi

trait TestingFramework {
  type R

  def name: String
  def runner: TestRunner[R]
  def filter: TestClassFilter
}
