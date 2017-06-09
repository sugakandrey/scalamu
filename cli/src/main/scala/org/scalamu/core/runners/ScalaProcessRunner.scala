package org.scalamu.core.runners

import java.io.{File, InputStream}
import java.net.ServerSocket
import java.nio.file.Path

import org.scalamu.core.CommunicationException
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.Worker

import scala.collection.mutable
import scala.concurrent.{blocking, ExecutionContext, Future}

trait ScalaProcessRunner[R] {
  def socket: ServerSocket
  def config: ScalamuConfig
  def worker: Worker[R]
  def connectionHandler: SocketConnectionHandler[R]
  def compiledSourcesDir: Path
  protected var proc: Process = _

  def execute()(
    implicit ec: ExecutionContext
  ): Future[Either[CommunicationException, List[R]]] = {
    val args    = List(socket.getLocalPort.toString)
    val builder = new ProcessBuilder(generateProcessArgs(worker, args): _*)
    configureProcessEnv(builder)
    builder.inheritIO()
    proc = builder.start()
    Future { blocking { connectionHandler.handle() } }
  }

  def kill(): Unit             = proc.destroyForcibly()
  def exitCode(): Int          = proc.waitFor()
  def errStream(): InputStream = proc.getErrorStream
  def outStream(): InputStream = proc.getInputStream

  private def configureProcessEnv(
    pb: ProcessBuilder
  ): Unit = {
    val classPathSegments = compiledSourcesDir :: (config.classPath | config.testClassDirs).toList
    val classPath  = classPathSegments.foldLeft("")(_ + _ + File.pathSeparator)
    val currentClassPath = System.getProperty("java.class.path")
    pb.environment().put("CLASSPATH", classPath + currentClassPath)
  }

  // @TODO Launch with -Djava.io.tmpdir, since scoverage is
  // @TODO not safe in case of multiple jvms
  private def generateProcessArgs(
                                   runner: Worker[_],
                                   runnerArgs: List[String]
  ): Seq[String] = {
    val args = mutable.ArrayBuffer.empty[String]
    args += config.scalaPath
    args ++= config.jvmArgs
    args += runner.name
    args ++= runnerArgs
    args
  }
}
