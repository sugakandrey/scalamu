package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.{ServerSocket, Socket}
import java.nio.file.Path

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.{MutationAnalysisWorker, _}
import org.scalamu.core.{CommunicationException, WorkerFailure}

class MutationAnalysisRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path,
  val workerId: Long
) extends Runner[(MutantId, Set[MeasuredSuite]), MutationAnalysisWorker.Result] {
  import MutationAnalysisRunner._

  override protected def worker: Worker[Result] = MutationAnalysisWorker

  private class MutationAnalysisCommunicationPipe(
    override val client: Socket,
    override val is: DataInputStream,
    override val os: DataOutputStream
  ) extends CommunicationPipe[Input, Result](client, is, os) {
    override def exchangeData(data: Input): Either[CommunicationException, Result] = {
      val tryExchange = super.exchangeData(data)
      tryExchange match {
        case Right(result) => Right(result)
        case Left(failure) => Left(failure)
        case Left(failure) if !proc.isAlive =>
          log.debug(
            s"Worker#$workerId was shut down due to $failure. Getting mutation#${data._1} status from exit code."
          )
          val exitCode = ExitCode.fromExitValue(exitValue())
          val status   = WorkerFailure.fromExitCode(exitCode)
          Right(MutationWorkerResponse(data._1, status))
      }
    }
  }

  override protected def connectionHandler: WorkerCommunicationHandler[Input, Result] =
    new WorkerCommunicationHandler[Input, Result](socket, sendConfigurationToWorker) {
      override def pipeFactory(client: Socket, is: DataInputStream, os: DataOutputStream) = super.pipeFactory(client, is, os)
    }

  override protected def sendConfigurationToWorker(os: DataOutputStream): Unit = {
    val configData = config.derive[MutationAnalysisWorkerConfig].asJson.noSpaces
    os.writeUTF(configData)
    os.flush()
  }

  override protected def generateProcessArgs(
    worker: Worker[_],
    executablePath: String,
    jvmArgs: Seq[String],
    runnerArgs: Seq[String]
  ): Seq[String] =
    super.generateProcessArgs(
      worker,
      executablePath,
      s"-Dworker.name=$workerId" +: jvmArgs,
      runnerArgs
    )
}

object MutationAnalysisRunner {
  private val log = Logger[MutationAnalysisRunner]
}
