package org.scalamu.testapi

import org.scalamu.core.ClassName

/**
 * Represents the result of s single test suite run.
 */
sealed trait TestSuiteResult {
  def succeeded: Boolean
}

final case class SuiteSuccess(name: ClassName, durationMillis: Long) extends TestSuiteResult {
  override def succeeded: Boolean = true
}

/**
 * Signals that a suite either was aborted or had failing tests.
 */
//sealed abstract class SuiteFailure extends TestSuiteResult(false)
sealed trait SuiteFailure extends TestSuiteResult {
  override def succeeded: Boolean = false
}

final case class TestsFailed(name: ClassName, failures: Seq[TestFailure])     extends SuiteFailure
final case class SuiteExecutionAborted(name: ClassName, errorMessage: String) extends SuiteFailure

object SuiteExecutionAborted {
  def apply(name: ClassName, error: Throwable): SuiteExecutionAborted =
    SuiteExecutionAborted(name, error.getMessage)
}
