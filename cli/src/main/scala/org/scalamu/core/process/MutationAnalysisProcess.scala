package org.scalamu.core.process

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners.{MutationRunner, Runner}
import org.scalamu.testapi.AbstractTestSuite

class MutationAnalysisProcess(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path,
  inverseCoverage: Map[MutantId, Set[AbstractTestSuite]]
) extends ScalaProcess[MutationRunner.Result] {

  override def runner: Runner[MutationRunner.Result] = MutationRunner

  override def connectionHandler: SocketConnectionHandler[MutationRunner.Result] =
    new RunnerCommunicationHandler[MutationRunner.Result](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val inverseCoverageBytes = inverseCoverage.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    os.writeInt(inverseCoverageBytes.length)
    os.write(inverseCoverageBytes)
    os.flush()
  }
}
