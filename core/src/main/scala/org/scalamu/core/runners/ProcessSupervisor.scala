package org.scalamu.core.runners

import java.io.InputStream
import java.util.concurrent.TimeUnit

class ProcessSupervisor[I, O](
  process: Process,
  val communicationPipe: CommunicationPipe[I, O]
) {

  def kill(): Unit                                     = process.destroyForcibly()
  def waitFor(): Int                                   = process.waitFor()
  def waitFor(time: Long, timeUnit: TimeUnit): Boolean = process.waitFor(time, timeUnit)
  def exitValue(): Int                                 = process.exitValue()
  def errStream(): InputStream                         = process.getErrorStream()
  def outStream(): InputStream                         = process.getInputStream()
  def isProcessAlive: Boolean                          = process.isAlive
}
