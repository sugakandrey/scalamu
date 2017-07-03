package org.scalamu.core.runners

import java.io._
import java.net.ServerSocket

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import org.scalamu.core.CommunicationException

abstract class SocketConnectionHandler[I: Encoder, O: Decoder] {
  def socket: ServerSocket

  def handle(): Either[CommunicationException, CommunicationPipe[I, O]] =
    Either.catchNonFatal {
      val client = socket.accept()
      val is     = new DataInputStream(new BufferedInputStream(client.getInputStream()))
      val os     = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()))
      new CommunicationPipe[I, O](client, is, os)
    }.leftMap(CommunicationException)
}
