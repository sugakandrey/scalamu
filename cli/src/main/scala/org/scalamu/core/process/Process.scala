package org.scalamu.core.process

import java.io.{Console => _, _}
import java.net.Socket

import io.circe.Encoder
import io.circe.syntax._
import org.scalamu.core.die
import org.scalamu.core.InternalFailure
import org.scalamu.core.runners._

abstract class Process[R: Encoder] {
  type Result = R

  type Configuration

  def name: String = getClass.getName.dropRight(1)

  protected def execute(args: Array[String]): Unit =
    tryWith(new Socket("localhost", args.head.toInt)) { socket =>
      val dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))
      val dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))

      val parseConfig = readConfigurationFromParent(dis)

      parseConfig match {
        case Left(parseError) =>
          Console.err.println(s"Error parsing worker configuration. $parseError")
          die(InternalFailure)
        case Right(config) =>
          run(config, dis, dos).foreach(result => { dos.writeUTF(result.asJson.noSpaces); dos.flush() })
      }
    }

  protected def readConfigurationFromParent(dis: DataInputStream): Either[Throwable, Configuration]

  protected def run(
    config: Configuration,
    dis: DataInputStream,
    dos: DataOutputStream
  ): Iterator[R]
}
