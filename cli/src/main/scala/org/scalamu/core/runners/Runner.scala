package org.scalamu.core.runners

import java.io.{DataOutputStream, File}
import java.net.ServerSocket
import java.nio.file.Path

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import org.scalamu.core.CommunicationException
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.process.Process

import scala.collection.mutable
import scala.util.Properties

abstract class Runner[I: Encoder, O: Decoder] {
  def config: ScalamuConfig
  def compiledSourcesDir: Path

  protected def socket: ServerSocket
  protected def worker: Process[O]
  protected def sendConfigurationToWorker(dos: DataOutputStream): Unit

  type Result = O
  type Input  = I

  protected val mainRunnerClass = "scala.tools.nsc.MainGenericRunner"

  protected def connectionHandler: SocketConnectionHandler[I, O] =
    new ProcessCommunicationHandler[I, O](socket, sendConfigurationToWorker)

  protected def javaExecutable: String =
    if (Properties.isWin) Properties.javaHome + """\bin\javaw.exe"""
    else Properties.javaHome + "/bin/java"

  def start(): Either[CommunicationException, ProcessSupervisor[I, O]] = {
    val args = List(socket.getLocalPort.toString)

    val builder = new ProcessBuilder(
      generateProcessArgs(worker, config.jvmOpts, args): _*
    )

    configureProcessEnv(builder)
    builder.inheritIO()
    for {
      proc <- Either.catchNonFatal(builder.start()).leftMap(CommunicationException)
      pipe <- connectionHandler.handle()
    } yield new ProcessSupervisor[I, O](proc, pipe)
  }

  protected def configureProcessEnv(
    pb: ProcessBuilder
  ): Unit = {
    val classPathSegments = compiledSourcesDir :: (config.classPath | config.testClassDirs).toList
    val classPath         = classPathSegments.foldLeft("")(_ + _ + File.pathSeparator)
    val currentClassPath  = Properties.javaClassPath
    pb.environment().put("CLASSPATH", classPath + currentClassPath)
  }

  protected def generateProcessArgs(
    worker: Process[_],
    jvmArgs: String,
    runnerArgs: Seq[String]
  ): Seq[String] = {
    val args = mutable.ArrayBuffer.empty[String]
    args += javaExecutable
    args += jvmArgs
    args += mainRunnerClass
    args += worker.name
    args ++= runnerArgs
    args
  }
}
