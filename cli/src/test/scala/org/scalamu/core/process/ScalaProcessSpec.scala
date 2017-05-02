package org.scalamu.core.process

import java.io.{DataInputStream, DataOutputStream, File}
import java.net.ServerSocket
import java.nio.file.{Path, Paths}

import org.scalamu.core.CommunicationException

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.testutil.ScalamuSpec
import org.scalamu.testutil.fixtures.ScalamuConfigFixture

class ScalaProcessSpec extends ScalamuSpec with ScalamuConfigFixture {
  override def scalaPath: String    = System.getenv("SCALA_HOME")
  override def jvmArgs: Seq[String] = Seq("-Xmx10m")
  override def classPath: Set[Path] =
    System
      .getProperty("java.class.path")
      .split(File.pathSeparator)
      .map(Paths.get(_))(collection.breakOut)

  object TestMain extends App {
    var buf = Set.empty[Array[Byte]]
    while (true) {
      buf += Array.ofDim(1024)
    }
  }

  "ScalaProcess" should "launch main class and wait for a process to die" in withConfig { cfg =>
    val handler = new SocketConnectionHandler[Int] {
      override def socket: ServerSocket                                      = ???
      override def initialize: (DataOutputStream) => Unit                    = ???
      override def receive(is: DataInputStream): Either[Throwable, Seq[Int]] = ???
      override def handle(): Either[CommunicationException, Seq[Int]]        = Right(Seq(42))
    }

    val proc = new ScalaProcess[Int] {
      override def port: Int                                       = -1
      override def socket: ServerSocket                            = ???
      override def config: ScalamuConfig                           = cfg
      override def target: Class[_]                                = TestMain.getClass
      override def connectionHandler: SocketConnectionHandler[Int] = handler
    }

    proc.execute().futureValue.right.value should ===(Seq(42))
    proc.exitCode() should ===(1)
  }
}
