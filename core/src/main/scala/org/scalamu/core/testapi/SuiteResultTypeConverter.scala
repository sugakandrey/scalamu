package org.scalamu.core.testapi

import org.scalamu.core.api.ClassName

/**
 * Allows to convert between internal, framework dependent suite
 * result type and [[org.scalamu.testapi.TestSuiteResult]]
 *
 * @tparam R Internal framework suite result type
 */
trait SuiteResultTypeConverter[R] {
  def fromResult(suite: ClassName)(result: R): TestSuiteResult
}
