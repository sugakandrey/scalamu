package org.scalamu.core.process

import java.io.{File, InputStream}
import java.net.ServerSocket

import org.scalamu.core.CommunicationException
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners.Runner

import scala.collection.mutable
import scala.concurrent.{blocking, ExecutionContext, Future}

trait ScalaProcess[R] {
  def socket: ServerSocket
  def config: ScalamuConfig
  def runner: Runner[R]
  def connectionHandler: SocketConnectionHandler[R]
  protected var proc: Process = _

  def execute()(
    implicit ec: ExecutionContext
  ): Future[Either[CommunicationException, Seq[R]]] = {
    val args    = List(socket.getLocalPort.toString)
    val builder = new ProcessBuilder(generateProcessArgs(runner, args): _*)
    configureProcessEnv(builder)
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
    val cp = config.classPath.foldLeft("")(_ + File.pathSeparator + _)
    pb.environment().put("CLASSPATH", cp)
  }

  // @TODO Launch with -Djava.io.tmpdir, since scoverage is
  // @TODO not safe in case of multiple jvms
  private def generateProcessArgs(
    runner: Runner[_],
    runnerArgs: List[String]
  ): Seq[String] = {
    val args = mutable.ArrayBuffer.empty[String]
    args += config.scalaPath.toString
    args ++= config.jvmArgs
    args += runner.name
    args ++= runnerArgs
    args
  }
}
