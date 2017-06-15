package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.{CoverageWorker, CoverageWorkerConfig, Worker}

class CoverageRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path
) extends ScalaProcessRunner[CoverageWorker.Result] {
  override val worker: Worker[CoverageWorker.Result] = CoverageWorker

  override val connectionHandler: SocketConnectionHandler[CoverageWorker.Result] =
    new WorkerCommunicationHandler[CoverageWorker.Result](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val configData        = config.derive[CoverageWorkerConfig].asJson.noSpaces
    val invocationDataDir = compiledSourcesDir.asJson.noSpaces
    os.writeUTF(configData)
    os.writeUTF(invocationDataDir)
    os.flush()
  }
}
