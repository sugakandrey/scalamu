package org.scalamu.core.process

import java.io._
import java.net.ServerSocket
import java.nio.file.{Path, Paths}

import org.scalamu.core.CommunicationException
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners.Runner
import org.scalamu.testutil.ScalamuSpec
import org.scalamu.testutil.fixtures.ScalamuConfigFixture

import scala.concurrent.ExecutionContext.Implicits.global

class ScalaProcessSpec extends ScalamuSpec with ScalamuConfigFixture {
  override def scalaPath: String = System.getenv("SCALA_HOME")
  override def classPath: Set[Path] =
    System
      .getProperty("java.class.path")
      .split(File.pathSeparator)
      .map(Paths.get(_))(collection.breakOut)

  "ScalaProcess" should "launch main class and wait for a process to die" in withConfig { cfg =>
    val handler = new SocketConnectionHandler[Int] {
      override def socket: ServerSocket                                       = ???
      override def initialize: (DataOutputStream) => Unit                     = ???
      override def receive(is: DataInputStream): Either[Throwable, List[Int]] = ???
      override def handle(): Either[CommunicationException, List[Int]]         = Right(List(42))
    }

    val proc = new ScalaProcess[Int] {
      override def socket: ServerSocket                            = new ServerSocket(1234)
      override def config: ScalamuConfig                           = cfg
      override def runner: Runner[Int]                             = TestMainSimple
      override def connectionHandler: SocketConnectionHandler[Int] = handler
    }

    proc.execute().futureValue.right.value should ===(Seq(42))
    proc.exitCode() should ===(123)
  }

  it should "send data to app JVM" in withConfig { cfg =>
    val serverSocket = new ServerSocket(4242)
    val handler      = new RunnerCommunicationHandler[Int](serverSocket, _ => ())

    val proc = new ScalaProcess[Int] {
      override def socket: ServerSocket                            = serverSocket
      override def config: ScalamuConfig                           = cfg
      override def runner: Runner[Int]                             = TestMainSending
      override def connectionHandler: SocketConnectionHandler[Int] = handler
    }

    proc.execute().futureValue.right.value should ===(1 to 10)
    proc.exitCode() should ===(0)
  }
}

object TestMainSimple extends Runner[Int] {
  def main(args: Array[String]): Unit =
    System.exit(123)

  override type Configuration = this.type
  override protected def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, TestMainSimple.type]                                     = ???
  override protected def run(configuration: TestMainSimple.type): Iterator[Int] = ???
}

object TestMainSending extends Runner[Int] {
  def main(args: Array[String]): Unit = execute(args)
  override type Configuration = Unit
  override protected def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Unit] = Right(())
  override protected def run(configuration: Unit): Iterator[Int] =
    (1 to 10).iterator
}
