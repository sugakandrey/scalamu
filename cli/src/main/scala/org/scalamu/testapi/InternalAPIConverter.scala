package org.scalamu.testapi

import org.scalamu.core.ClassName

trait InternalAPIConverter[R] {
  def fromResult(suite: ClassName)(result: R): TestSuiteResult
}
