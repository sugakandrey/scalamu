package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.{MeasuredSuite, MutationAnalysisWorker, Worker}

class MutationAnalysisRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path,
  inverseCoverage: Map[MutantId, Set[MeasuredSuite]]
) extends ScalaProcessRunner[MutationAnalysisWorker.Result] {

  override def worker: Worker[MutationAnalysisWorker.Result] = MutationAnalysisWorker

  override def connectionHandler: SocketConnectionHandler[MutationAnalysisWorker.Result] =
    new WorkerCommunicationHandler[MutationAnalysisWorker.Result](socket, sendDataToRunner)

  private def sendDataToRunner(os: DataOutputStream): Unit = {
    val inverseCoverageBytes = inverseCoverage.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    os.writeInt(inverseCoverageBytes.length)
    os.write(inverseCoverageBytes)
    os.flush()
  }
}
