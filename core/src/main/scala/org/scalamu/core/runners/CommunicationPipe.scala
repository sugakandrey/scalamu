package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket

import cats.syntax.either._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import org.scalamu.core.api.CommunicationException

class CommunicationPipe[I: Encoder, O: Decoder](
  val client: Socket,
  val is: DataInputStream,
  val os: DataOutputStream
) {

  def send(data: I): Unit = {
    os.writeUTF(data.asJson.noSpaces)
    os.flush()
  }

  def receive(): Either[CommunicationException, O] =
    (for {
      message <- Either.catchNonFatal(is.readUTF())
      data    <- decode[O](message)
    } yield data).leftMap(CommunicationException)

  def close(): Unit = client.close()
}
