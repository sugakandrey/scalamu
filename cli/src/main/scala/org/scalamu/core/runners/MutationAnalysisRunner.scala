package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.ServerSocket
import java.nio.file.Path

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.{Untested, WorkerFailure}
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers._

import scala.collection.mutable

class MutationAnalysisRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path,
  mutationCoverage: Map[MutantId, Set[MeasuredSuite]]
) extends ScalaProcessRunner[MutationAnalysisWorker.Result] {
  private val runnerQueue =
    mutable.Queue[(MutantId, Set[MeasuredSuite])](mutationCoverage.toSeq: _*)

  override def worker: Worker[MutationAnalysisWorker.Result] = MutationAnalysisWorker

  override def connectionHandler: SocketConnectionHandler[MutationAnalysisWorker.Result] =
    new WorkerCommunicationHandler[MutationAnalysisWorker.Result](socket, sendDataToWorker) {
      override protected def exchangeData(
        is: DataInputStream,
        os: DataOutputStream
      ): Either[Throwable, String] = Either.catchNonFatal {
        val currentMutation = runnerQueue.head
        os.writeUTF(currentMutation.asJson.noSpaces)
        os.flush()
        val status = is.readUTF()
        runnerQueue.dequeue()
        status
      }

      override def communicate(
        is: DataInputStream,
        os: DataOutputStream
      ): Either[Throwable, List[MutationAnalysisWorker.Result]] = {
        val testedMutations = super.communicate(is, os)

        testedMutations.map { tested =>
          if (tested.size == mutationCoverage.size) {
            tested
          } else {
            val current        = runnerQueue.dequeue()
            val exitCode       = ExitCode.fromExitValue(proc.waitFor())
            val status         = WorkerFailure.fromExitCode(exitCode)
            val failedMutation = MutationWorkerResponse(current._1, status)

            val untested: List[MutationWorkerResponse] =
              runnerQueue.map { case (id, _) => MutationWorkerResponse(id, Untested) }(collection.breakOut)

            failedMutation :: untested ::: tested
          }
        }
      }
    }

  private def sendDataToWorker(os: DataOutputStream): Unit = {
    val configData = config.derive[MutationAnalysisWorkerConfig].asJson.noSpaces
    os.writeUTF(configData)
    os.flush()
  }
}
