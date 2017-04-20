package org.scalamu.testapi

import org.scalamu.core.ClassName

sealed trait TestSuiteResult

object TestSuiteResult {
  final case class Aborted(suite: ClassName, cause: Throwable)          extends TestSuiteResult
  final case class Successful(suite: ClassName, durationMillis: Long)   extends TestSuiteResult
  final case class Failed(suite: ClassName, failures: Seq[TestFailure]) extends TestSuiteResult
}
