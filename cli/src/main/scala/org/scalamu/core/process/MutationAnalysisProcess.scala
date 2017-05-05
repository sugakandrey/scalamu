package org.scalamu.core.process

import java.io.DataOutputStream
import java.net.ServerSocket

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners.{MutantId, MutationRunner, Runner}
import org.scalamu.testapi.AbstractTestSuite

class MutationAnalysisProcess(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  compiledSources: Map[String, Array[Byte]],
  inverseCoverage: Map[MutantId, Set[AbstractTestSuite]]
) extends ScalaProcess[MutationRunner.Result] {

  override def runner: Runner[MutationRunner.Result] = MutationRunner

  override def connectionHandler: SocketConnectionHandler[MutationRunner.Result] =
    new RunnerCommunicationHandler[MutationRunner.Result](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val inverseCoverageJson = inverseCoverage.asJson.noSpaces
    os.writeUTF(inverseCoverageJson)
    compiledSources.foreach {
      case (name, bytes) =>
        os.writeUTF(name)
        os.write(bytes.length)
        os.write(bytes)
    }
    os.flush()
  }
}
