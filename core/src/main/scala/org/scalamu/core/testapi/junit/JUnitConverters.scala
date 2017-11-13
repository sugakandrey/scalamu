package org.scalamu.core.testapi
package junit

import cats.implicits._
import org.junit.runner.Result
import org.junit.runner.notification.{Failure => JUFailure}
import org.scalamu.core.api.ClassName

import scala.collection.JavaConverters._

case object JUnitConverters extends SuiteResultTypeConverter[Result] {
  private val toFailureMessage: JUFailure => String = _.toString
  private val toException: JUFailure => Throwable   = _.getException

  private val juFailureToTestFailure: JUFailure => TestFailure =
    (toFailureMessage, toException andThen { _.getMessage.some }).mapN(TestFailure)

  override def fromResult(suite: ClassName)(result: Result): TestSuiteResult =
    if (result.wasSuccessful())
      SuiteSuccess(suite, result.getRunTime)
    else
      TestsFailed(
        suite,
        result.getFailures.asScala.map(juFailureToTestFailure)
      )
}
