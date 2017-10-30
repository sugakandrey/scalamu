package org.scalamu.core
package runners

import java.io.{DataOutputStream, File}
import java.net.ServerSocket
import java.nio.file.Path

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, Encoder}
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.process.Process

import scala.collection.mutable
import scala.util.Properties

abstract class Runner[I: Encoder, O: Decoder] {
  import Runner._
  
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
    val mainArgs = List(socket.getLocalPort.toString)
    val args     = generateProcessArgs(worker, config.vmParameters, mainArgs)
    val builder  = new ProcessBuilder(args: _*)
    configureProcessEnv(builder)
    builder.inheritIO()
    log.debug(s"Configured builder: ${builder.command()}.")
    for {
      proc <- Either.catchNonFatal(builder.start()).leftMap(CommunicationException)
      pipe <- connectionHandler.handle()
    } yield new ProcessSupervisor[I, O](proc, pipe)
  }

  protected def configureProcessEnv(
    pb: ProcessBuilder
  ): Unit = {
    val classPathSegments = compiledSourcesDir :: config.testClassPath.toList
    val testClassPath     = pathsToString(classPathSegments)
    val currentClassPath  = Properties.javaClassPath
    val configuredClasspath = currentClassPath + File.pathSeparator + testClassPath
    log.debug(s"Configured classpath: $configuredClasspath")
    pb.environment().put("CLASSPATH", configuredClasspath)
  }

  protected def generateProcessArgs(
    worker: Process[_],
    jvmArgs: String,
    runnerArgs: Seq[String]
  ): Seq[String] = {
    val args = mutable.ArrayBuffer.empty[String]
    args += javaExecutable
    if (jvmArgs.nonEmpty) {
      args += jvmArgs
    }
    args += mainRunnerClass
    args += worker.name
    args ++= runnerArgs
    args
  }
}

object Runner {
  private val log = Logger[Runner.type]
}
