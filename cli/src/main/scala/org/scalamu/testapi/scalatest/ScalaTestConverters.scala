package org.scalamu.testapi
package scalatest

import org.scalamu.core.ClassName
import org.scalatest.events.{RunCompleted, RunStarting, SuiteAborted, TestFailed}
import org.scalatest.{Reporter, Status}

import scala.collection.mutable

class ScalaTestConverters extends SuiteResultTypeConverter[Status] {
  private val failures                  = mutable.Set.empty[TestFailure]
  private var startTimestamp: Long      = _
  private var completionTimestamp: Long = _

  private[scalatest] val reporter: Reporter = {
    case e: TestFailed   => failures += TestFailure(e.message, e.throwable.map(_.getMessage))
    case e: SuiteAborted => failures += TestFailure(e.message, e.throwable.map(_.getMessage))
    case e: RunStarting  => startTimestamp = e.timeStamp
    case e: RunCompleted => completionTimestamp = e.timeStamp
    case _               =>
  }

  override def fromResult(suite: ClassName)(result: Status): TestSuiteResult =
    if (result.succeeds())
      SuiteSuccess(suite, completionTimestamp - startTimestamp)
    else
      TestsFailed(suite, failures.toSeq)

}
