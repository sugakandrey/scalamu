package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.process.{CoverageProcess, CoverageProcessConfig, Process}

class CoverageRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path
) extends Runner[Nothing, CoverageProcess.Result] {
  override protected def worker: Process[CoverageProcess.Result] = CoverageProcess

  override protected def sendConfigurationToWorker(dos: DataOutputStream): Unit = {
    val configData        = config.derive[CoverageProcessConfig].asJson.noSpaces
    val invocationDataDir = compiledSourcesDir.asJson.noSpaces
    dos.writeUTF(configData)
    dos.writeUTF(invocationDataDir)
    dos.flush()
  }
}
