package org.scalamu.testapi

import org.scalamu.core.ClassFileInfo

trait TestRunner[R] {
  def run(testClass: ClassFileInfo): TestSuiteResult
  protected def converter: InternalAPIConverter[R]
}
