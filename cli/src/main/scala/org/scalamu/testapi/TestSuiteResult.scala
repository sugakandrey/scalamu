package org.scalamu.testapi

import org.scalamu.core.ClassName

/**
 * Represents the result of s single test suite run.
 */
sealed abstract class TestSuiteResult(succeeded: Boolean)

/**
 * Signals that a suite either was aborted or has failed tests.
 */
sealed abstract class SuiteFailure extends TestSuiteResult(false)

object TestSuiteResult {
  final case class Success(name: ClassName, durationMillis: Long)           extends TestSuiteResult(true)
  final case class Aborted(name: ClassName, cause: Throwable)               extends SuiteFailure
  final case class TestsFailed(name: ClassName, failures: Seq[TestFailure]) extends SuiteFailure
}
