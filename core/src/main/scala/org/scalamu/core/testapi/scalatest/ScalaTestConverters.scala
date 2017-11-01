package org.scalamu.core.testapi
package scalatest

import org.scalamu.core.api.ClassName
import org.scalatest.events._
import org.scalatest.{Reporter, Status, Stopper}

import scala.collection.mutable

class ScalaTestConverters extends SuiteResultTypeConverter[Status] {
  private val failures       = mutable.Set.empty[TestFailure]
  private var duration: Long = _

  private def failureMessage(e: TestFailed): String =
    s"Test ${e.testName} in suite ${e.suiteName} failed."

  private[scalatest] val reporter: Reporter = {
    case e: TestFailed    => failures += TestFailure(failureMessage(e), e.throwable.map(_.getMessage))
    case e: SuiteAborted  => failures += TestFailure(e.message, e.throwable.map(_.getMessage))
    case e: TestSucceeded => e.duration.foreach(duration += _)
    case _                =>
  }

  private[scalatest] val stopper: Stopper = new Stopper {
    override def stopRequested: Boolean = failures.nonEmpty
    override def requestStop(): Unit    = ()
  }

  override def fromResult(suite: ClassName)(result: Status): TestSuiteResult =
    if (result.succeeds())
      SuiteSuccess(suite, duration)
    else
      TestsFailed(suite, failures.toSeq)

}
