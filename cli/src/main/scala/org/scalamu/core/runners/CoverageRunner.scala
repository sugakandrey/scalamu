package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.{ServerSocket, Socket}
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.{CoverageWorker, CoverageWorkerConfig, Worker}

class CoverageRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path
) extends Runner[Nothing, CoverageWorker.Result] {
  override protected def worker: Worker[CoverageWorker.Result] = CoverageWorker

  override protected def sendConfigurationToWorker(dos: DataOutputStream): Unit = {
    val configData        = config.derive[CoverageWorkerConfig].asJson.noSpaces
    val invocationDataDir = compiledSourcesDir.asJson.noSpaces
    dos.writeUTF(configData)
    dos.writeUTF(invocationDataDir)
    dos.flush()
  }

  override protected def connectionHandler: WorkerCommunicationHandler[Nothing, CoverageWorker.Result] =
    new WorkerCommunicationHandler[Nothing, Result](socket, sendConfigurationToWorker) {
      override def pipeFactory(client: Socket, is: DataInputStream, os: DataOutputStream) =
        new CommunicationPipe[Nothing, Result](client, is, os) {
          override protected def send(data: Nothing): Unit = ()
        }
    }
}
