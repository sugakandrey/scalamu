package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket

import org.scalamu.core.workers.CoverageWorker

class CoverageCommunicationPipe(
  override val client: Socket,
  override val is: DataInputStream,
  override val os: DataOutputStream
) extends CommunicationPipe[Nothing, CoverageWorker.Result](client, is, os) {
  override protected def send(data: Nothing): Unit = ()
}
