package org.scalamu

import org.scalamu.core.workers.ExitCode

package object core {
  def exit(log: String => Unit)(exitMessage: String, exitCode: ExitCode): Nothing = {
    log(exitMessage)
    exit(exitCode)
  }
  
  def exit(exitCode: ExitCode): Nothing = sys.exit(exitCode.code)
}
