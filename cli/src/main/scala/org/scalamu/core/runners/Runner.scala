package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream, File, InputStream}
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.{Decoder, Encoder}
import org.scalamu.core.CommunicationException
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.Worker

import scala.collection.mutable

abstract class Runner[I: Encoder, R: Decoder] {
  def config: ScalamuConfig
  def compiledSourcesDir: Path

  protected def socket: ServerSocket
  protected def worker: Worker[R]
  protected def sendConfigurationToWorker(dos: DataOutputStream): Unit

  type Result = R
  type Input  = I

  protected var proc: Process = _

  protected def connectionHandler: SocketConnectionHandler[I, R]

  def start(): Either[CommunicationException, CommunicationPipe[I, R]] = {
    val args = List(socket.getLocalPort.toString)

    val builder = new ProcessBuilder(
      generateProcessArgs(worker, config.scalaPath, config.jvmArgs, args): _*
    )

    configureProcessEnv(builder)
    builder.inheritIO()
    proc = builder.start()
    connectionHandler.handle()
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
