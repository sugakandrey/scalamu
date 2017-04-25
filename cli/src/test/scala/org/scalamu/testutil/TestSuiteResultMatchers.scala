package org.scalamu.testutil

import org.scalamu.testapi.TestSuiteResult
import org.scalamu.testapi.TestSuiteResult._
import org.scalatest.Matchers
import org.scalatest.matchers.{BeMatcher, BePropertyMatchResult, BePropertyMatcher, MatchResult}

trait TestSuiteResultMatchers extends Matchers {

  class SuccessBePropMatcher extends BePropertyMatcher[TestSuiteResult] {
    override def apply(result: TestSuiteResult): BePropertyMatchResult =
      BePropertyMatchResult(
        result.isInstanceOf[Success],
        "successful"
      )
  }

  class FailureBePropMatcher extends BePropertyMatcher[TestSuiteResult] {
    override def apply(result: TestSuiteResult): BePropertyMatchResult =
      BePropertyMatchResult(
        result.isInstanceOf[TestsFailed],
        "failed"
      )
  }

  class AbortBeMatcher extends BeMatcher[TestSuiteResult] {
    override def apply(result: TestSuiteResult): MatchResult =
      MatchResult(
        result.isInstanceOf[Aborted],
        s"Execution of $result has not been aborted.",
        s"Execution of $result has been aborted."
      )
  }

  val success = new SuccessBePropMatcher
  val failure = new FailureBePropMatcher
  val aborted = new AbortBeMatcher
}
