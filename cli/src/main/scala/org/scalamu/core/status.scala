package org.scalamu.core

/**
 * Represents the status of a single mutant, after mutation analysis is completed.
 *
 * @param killed was the mutant killed
 */
sealed abstract class DetectionStatus(val killed: Boolean)

/**
 * Base class for statuses related to runtime failures.
 */
sealed abstract class WorkerFailure extends DetectionStatus(false)

case object TimedOut       extends WorkerFailure
case object OutOfMemory    extends WorkerFailure
case object RuntimeFailure extends WorkerFailure

final case class Killed(killingTest: ClassName) extends DetectionStatus(true) {
  override def toString: String = s"Killed by ${killingTest.fullName}"
}

case object Alive          extends DetectionStatus(false)
case object NoTestCoverage extends DetectionStatus(false)
case object Untested       extends DetectionStatus(false)
