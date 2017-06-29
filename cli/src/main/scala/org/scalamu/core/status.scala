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
sealed abstract class RemoteProcessFailure(val exitCode: Int) extends DetectionStatus(false)

object RemoteProcessFailure {
  def fromExitValue(exitValue: Int): RemoteProcessFailure = exitValue match {
    case 1  => InternalFailure
    case 48 => TimedOut
    case 49 => OutOfMemory
    case _  =>
      throw new IllegalArgumentException(
        s"Can't construct RemoteProcessFailure from exit value: $exitValue."
      )
  }
}

case object TimedOut        extends RemoteProcessFailure(48)
case object OutOfMemory     extends RemoteProcessFailure(49)
case object InternalFailure extends RemoteProcessFailure(1)

final case class Killed(killingTest: ClassName) extends DetectionStatus(true) {
  override def toString: String = s"Killed by ${killingTest.fullName}"
}

case object Alive          extends DetectionStatus(false)
case object NoTestCoverage extends DetectionStatus(false)
case object Untested       extends DetectionStatus(false)
