package org.scalamu

package object core {
  def die(failure: RemoteProcessFailure): Nothing = die(failure.exitCode)
  def die(exitValue: Int): Nothing                = sys.exit(exitValue)
}
