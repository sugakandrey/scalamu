package org.scalamu.testapi.utest

import org.scalamu.core.ClassName
import org.scalamu.testapi.{SuiteResultTypeConverter, TestFailure, TestSuiteResult}
import utest.framework.{Result, Tree}

object UTestConverters extends SuiteResultTypeConverter[Tree[Result]] {
  private def fromFailure(failure: Result): TestFailure =
    TestFailure(s"Test ${failure.name} failed.", failure.value.failed.toOption)

  override def fromResult(suite: ClassName)(result: Tree[Result]): TestSuiteResult = {
    val results = result.toSeq
    if (results.forall(_.value.isSuccess))
      TestSuiteResult.Successful(suite, results.map(_.milliDuration).sum)
    else
      TestSuiteResult.Failed(
        suite,
        results.collect {
          case r if r.value.isFailure => fromFailure(r)
        }
      )
  }
}
