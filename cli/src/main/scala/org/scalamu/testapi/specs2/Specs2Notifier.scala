package org.scalamu.testapi
package specs2

import java.util.Date

import org.specs2.execute.Details
import org.specs2.reporter.SilentNotifier

import scala.collection.mutable

class Specs2Notifier extends SilentNotifier {
  private var startTimestamp: Long      = _
  private var completionTimestamp: Long = _

  def duration: Long = completionTimestamp - startTimestamp

  private val failures = mutable.Set.empty[TestFailure]

  private def addFailure(message: String, throwable: Throwable): Unit =
    failures += TestFailure(message, Some(throwable.getMessage))

  def getFailures: Seq[TestFailure] = failures.toSeq

  override def specStart(title: String, location: String): Unit =
    startTimestamp = new Date().getTime

  override def specEnd(title: String, location: String): Unit =
    completionTimestamp = new Date().getTime

  override def stepError(message: String, location: String, f: Throwable, duration: Long): Unit =
    addFailure(message, f)

  override def exampleFailure(
    name: String,
    message: String,
    location: String,
    f: Throwable,
    details: Details,
    duration: Long
  ): Unit = addFailure(s"Example $name failed. $message", f)

  override def exampleError(
    name: String,
    message: String,
    location: String,
    f: Throwable,
    duration: Long
  ): Unit = addFailure(s"Error $f during execution of example $name.", f)
}
