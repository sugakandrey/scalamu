package org.scalamu.core.process

import java.io.DataOutputStream
import java.net.ServerSocket

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.InstrumentationReporter
import org.scalamu.core.process.CoverageRunnerConfig._
import org.scalamu.core.runners.{CoverageRunner, Runner}

class CoverageProcess(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  compiledSources: Map[String, Array[Byte]],
  reporter: InstrumentationReporter
) extends ScalaProcess[CoverageRunner.Result] {
  override val runner: Runner[CoverageRunner.Result] = CoverageRunner

  override val connectionHandler: SocketConnectionHandler[CoverageRunner.Result] =
    new RunnerCommunicationHandler[CoverageRunner.Result](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val configData         = config.derive[CoverageRunnerConfig].asJson.noSpaces
    val statementsByIdData = reporter.asJson.noSpaces
    os.writeUTF(configData)
    os.writeUTF(statementsByIdData)
    compiledSources.foreach {
      case (name, bytes) =>
        os.writeUTF(name)
        os.writeInt(bytes.length)
        os.write(bytes)
    }
    os.flush()
  }
}
