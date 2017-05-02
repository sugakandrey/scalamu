package org.scalamu.core.process

import java.io.DataOutputStream
import java.net.ServerSocket

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners.MutationRunner
import org.scalamu.plugin.MutantInfo
import org.scalamu.testapi.AbstractTestSuite

class MutationAnalysisProcess(
  override val port: Int,
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  inverseCoverage: Map[MutantInfo, Set[AbstractTestSuite]]
) extends ScalaProcess[MutationRunner.ProcessData] {

  import MutationRunner.ProcessData

  override def target: Class[_] = MutationRunner.getClass

  override def connectionHandler: SocketConnectionHandler[ProcessData] =
    new RunnerCommunicationHandler[ProcessData](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    implicitly[io.circe.Encoder[MutantInfo]]
    val mutants = inverseCoverage.keysIterator.zipWithIndex.toMap
    val coverageByMutantId = inverseCoverage.map {
      case (k, v) => mutants(k) -> v
    }
    val coverageJson = coverageByMutantId.asJson.noSpaces
    os.writeUTF(coverageJson)
    val mutantsJson = mutants.map(_.swap).asJson.noSpaces
    os.writeUTF(mutantsJson)
    os.flush()
  }
}
