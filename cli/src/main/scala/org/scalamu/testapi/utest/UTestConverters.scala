package org.scalamu.testapi
package utest

import _root_.utest.framework.{Result, Tree}
import org.scalamu.core.ClassName

object UTestConverters extends SuiteResultTypeConverter[Tree[Result]] {
  private def fromFailure(failure: Result): TestFailure =
    TestFailure(s"Test ${failure.name} failed.", failure.value.failed.map(_.getMessage).toOption)

  override def fromResult(suite: ClassName)(result: Tree[Result]): TestSuiteResult = {
    val results = result.toSeq
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
