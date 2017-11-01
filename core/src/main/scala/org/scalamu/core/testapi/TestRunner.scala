package org.scalamu.core.testapi

import org.scalamu.core.api.ClassName

/**
 * Provides an entry point for execution of a single test suite.
 *
 * @tparam R framework internal suite result representation type
 */
trait TestRunner[R] {
  def arguments: String
  def run(suite: ClassName): TestSuiteResult
  protected def converter: SuiteResultTypeConverter[R]
}
