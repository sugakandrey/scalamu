package org.scalamu.testapi

import org.scalamu.core.ClassInfo

/**
 * Provides an entry point for execution of a single test suite.
 *
 * @tparam R framework internal suite result representation type
 */
trait TestRunner[R] {
  def run(testClass: ClassInfo): TestSuiteResult
  protected def converter: SuiteResultTypeConverter[R]
}
