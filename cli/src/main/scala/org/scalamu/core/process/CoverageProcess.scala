package org.scalamu.core.process

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners.CoverageRunnerConfig._
import org.scalamu.core.runners.{CoverageRunner, CoverageRunnerConfig, Runner}

class CoverageProcess(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path
) extends ScalaProcess[CoverageRunner.Result] {
  override val runner: Runner[CoverageRunner.Result] = CoverageRunner

  override val connectionHandler: SocketConnectionHandler[CoverageRunner.Result] =
    new RunnerCommunicationHandler[CoverageRunner.Result](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val configData        = config.derive[CoverageRunnerConfig].asJson.noSpaces
    val invocationDataDir = compiledSourcesDir.asJson.noSpaces
    os.writeUTF(configData)
    os.writeUTF(invocationDataDir)
    os.flush()
  }
}
