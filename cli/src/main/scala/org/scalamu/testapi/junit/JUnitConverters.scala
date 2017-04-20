package org.scalamu.testapi.junit

import org.junit.runner.Result
import org.junit.runner.notification.{Failure => JUFailure}
import org.scalamu.testapi.{InternalAPIConverter, TestFailure, TestSuiteResult}
import cats.instances.function._
import cats.syntax.cartesian._
import cats.syntax.option._
import org.scalamu.core.ClassName

import scala.collection.JavaConverters._

case object JUnitConverters extends InternalAPIConverter[Result] {
  private val toFailureMessage: JUFailure => String = _.toString
  private val toException: JUFailure => Throwable   = _.getException

  private val juFailureToTestFailure: JUFailure => TestFailure = {
    toFailureMessage |@| (toException andThen { _.some })
  }.map(TestFailure)

  override def fromResult(suite: ClassName)(result: Result): TestSuiteResult =
    if (result.wasSuccessful())
      TestSuiteResult.Successful(suite, result.getRunTime)
    else
      TestSuiteResult.Failed(
        suite,
        result.getFailures.asScala.map(juFailureToTestFailure)
      )
}
