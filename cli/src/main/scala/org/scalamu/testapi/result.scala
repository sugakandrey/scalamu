package org.scalamu.testapi

import io.circe.{Decoder, Encoder}
import org.scalamu.core.ClassName

/**
 * Represents the result of s single test suite run.
 */
sealed abstract class TestSuiteResult(val succeeded: Boolean)

final case class SuiteSuccess(name: ClassName, durationMillis: Long) extends TestSuiteResult(true)

/**
 * Signals that a suite either was aborted or had failing tests.
 */
sealed abstract class SuiteFailure extends TestSuiteResult(false)

final case class TestsFailed(name: ClassName, failures: Seq[TestFailure])     extends SuiteFailure
final case class SuiteExecutionAborted(name: ClassName, errorMessage: String) extends SuiteFailure

object SuiteExecutionAborted {
  def apply(name: ClassName, error: Throwable): SuiteExecutionAborted =
    SuiteExecutionAborted(name, error.getMessage)
}

// This exists for cross building against scala 2.11, since circe won't work for me out of the box
trait TestSuiteResultImplicits {
  implicit val decodeResult: Decoder[TestSuiteResult] = implicitly[Decoder[TestSuiteResult]]
  implicit val encodeResult: Encoder[TestSuiteResult] = implicitly[Encoder[TestSuiteResult]]
}
