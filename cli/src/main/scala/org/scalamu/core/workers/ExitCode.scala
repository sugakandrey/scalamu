package org.scalamu.core.workers

sealed abstract class ExitCode(val code: Int)

object ExitCode {
  case object Ok             extends ExitCode(0)
  case object OutOfMemory    extends ExitCode(49)
  case object TimedOut       extends ExitCode(48)
  case object RuntimeFailure extends ExitCode(1)

  def fromExitValue(code: Int): ExitCode = code match {
    case 0  => Ok
    case 1  => RuntimeFailure
    case 48 => TimedOut
    case 49 => OutOfMemory
    case _  => throw new IllegalArgumentException(s"Unknown exit code: $code")
  }
}
