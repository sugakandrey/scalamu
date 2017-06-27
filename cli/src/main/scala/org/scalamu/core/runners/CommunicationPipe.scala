package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket

import cats.syntax.either._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import org.scalamu.core.CommunicationException

class CommunicationPipe[I: Encoder, O: Decoder](
  val client: Socket,
  val is: DataInputStream,
  val os: DataOutputStream
) {

  protected def send(data: I): Unit = os.writeUTF(data.asJson.noSpaces)

  protected def receive(): Either[CommunicationException, O] =
    decode[O](is.readUTF()).leftMap(CommunicationException)

  def exchangeData(data: I): Either[CommunicationException, O] = {
    send(data)
    receive()
  }
  
  def close(): Unit = client.close()
}
