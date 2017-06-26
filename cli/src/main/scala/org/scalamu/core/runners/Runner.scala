package org.scalamu.core.runners

import java.io.{DataOutputStream, File, InputStream}
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.Decoder
import org.scalamu.core.CommunicationException
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.Worker

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, blocking}

abstract class Runner[R: Decoder] {
  def config: ScalamuConfig
  def compiledSourcesDir: Path

  protected def socket: ServerSocket
  protected def worker: Worker[R]
  protected def sendDataToWorker(dos: DataOutputStream): Unit

  protected var proc: Process = _

  protected def connectionHandler: SocketConnectionHandler[R] =
    new WorkerCommunicationHandler[R](socket, sendDataToWorker)

  def execute()(
    implicit ec: ExecutionContext
  ): Future[Either[CommunicationException, List[R]]] = {
    val args = List(socket.getLocalPort.toString)

    val builder = new ProcessBuilder(
      generateProcessArgs(worker, config.scalaPath, config.jvmArgs, args): _*
    )

    configureProcessEnv(builder)
    builder.inheritIO()
    proc = builder.start()
    Future { blocking { connectionHandler.handle() } }
  }

  def kill(): Unit             = proc.destroyForcibly()
  def exitValue(): Int         = proc.waitFor()
  def errStream(): InputStream = proc.getErrorStream
  def outStream(): InputStream = proc.getInputStream

  protected def configureProcessEnv(
    pb: ProcessBuilder
  ): Unit = {
    val classPathSegments = compiledSourcesDir :: (config.classPath | config.testClassDirs).toList
    val classPath         = classPathSegments.foldLeft("")(_ + _ + File.pathSeparator)
    val currentClassPath  = System.getProperty("java.class.path")
    pb.environment().put("CLASSPATH", classPath + currentClassPath)
  }

  protected def generateProcessArgs(
    worker: Worker[_],
    executablePath: String,
    jvmArgs: Seq[String],
    runnerArgs: Seq[String]
  ): Seq[String] = {
    val args = mutable.ArrayBuffer.empty[String]
    args += executablePath
    args ++= jvmArgs
    args += worker.name
    args ++= runnerArgs
    args
  }
}
