package org.scalamu.core.process

import java.io.DataOutputStream
import java.net.ServerSocket

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.Statement
import org.scalamu.core.process.CoverageRunnerConfig._
import org.scalamu.core.runners.CoverageRunner

class CoverageProcess(
  override val port: Int,
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  statementsById: Map[Int, Statement]
) extends ScalaProcess[CoverageRunner.ProcessData] {
  import CoverageRunner.ProcessData

  override val target: Class[_] = CoverageRunner.getClass

  override val connectionHandler: SocketConnectionHandler[ProcessData] =
    new RunnerCommunicationHandler[ProcessData](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val configData         = config.derive[CoverageRunnerConfig].asJson.noSpaces
    val statementsByIdData = statementsById.asJson.noSpaces
    os.writeUTF(configData)
    os.writeUTF(statementsByIdData)
    os.flush()
  }
}
