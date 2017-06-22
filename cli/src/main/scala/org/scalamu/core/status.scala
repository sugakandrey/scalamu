package org.scalamu.core

import org.scalamu.core.workers.ExitCode

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

object WorkerFailure {
  def fromExitCode(code: ExitCode): DetectionStatus = code match {
    case ExitCode.TimedOut       => TimedOut
    case ExitCode.OutOfMemory    => OutOfMemory
    case ExitCode.RuntimeFailure => InternalFailure
    case ExitCode.Ok             => throw new IllegalArgumentException(s"Can't construct WorkerFailure from Ok exit code.")
  }
}

case object TimedOut        extends WorkerFailure
case object OutOfMemory     extends WorkerFailure
case object InternalFailure extends WorkerFailure

final case class Killed(killingTest: ClassName) extends DetectionStatus(true) {
  override def toString: String = s"Killed by ${killingTest.fullName}"
}

case object Alive          extends DetectionStatus(false)
case object NoTestCoverage extends DetectionStatus(false)
case object Untested       extends DetectionStatus(false)
