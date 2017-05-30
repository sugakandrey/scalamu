package org.scalamu.core.runners

import java.io._
import java.net.Socket

import cats.syntax.either._
import io.circe.Encoder
import io.circe.syntax._
import org.scalamu.core.process._

trait Runner[R] {
  type Result = R

  type Configuration

  def name: String = getClass.getName.dropRight(1)

  protected def execute(
    args: Array[String]
  )(implicit encoder: Encoder[R]): Unit =
    tryWith(new Socket("localhost", args.head.toInt)) { socket =>
      val dis = new DataInputStream(new BufferedInputStream(socket.getInputStream))
      val dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream))

      readConfigurationFromParent(dis).bimap(
        err => { println(err); System.exit(1) },
        run _ andThen { it => it.foreach(data => dos.writeUTF(data.asJson.noSpaces)); dos.flush() }
      )
    }

  protected def readCompiledSources(dis: DataInputStream): Map[String, Array[Byte]] =
    Iterator.continually {
      val name   = dis.readUTF()
      val length = dis.readInt()
      val bytes  = Array.ofDim[Byte](length)
      dis.readFully(bytes)
      name -> bytes
    }.toMap

  protected def readConfigurationFromParent(dis: DataInputStream): Either[Throwable, Configuration]

  protected def run(configuration: Configuration): Iterator[R]
}
