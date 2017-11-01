package org.scalamu.core.testapi
package utest

import _root_.utest.framework.{HTree, Result}
import org.scalamu.core.api.ClassName

object UTestConverters extends SuiteResultTypeConverter[HTree[String, Result]] {
  private def fromFailure(failure: Result): TestFailure =
    TestFailure(s"Test ${failure.name} failed.", failure.value.failed.map(_.getMessage).toOption)

  override def fromResult(suite: ClassName)(result: HTree[String, Result]): TestSuiteResult = {
    val results = result.leaves.toSeq
    if (results.forall(_.value.isSuccess))
      SuiteSuccess(suite, results.map(_.milliDuration).sum)
    else
      TestsFailed(
        suite,
        results.collect {
          case r if r.value.isFailure => fromFailure(r)
        }
      )
  }
}
