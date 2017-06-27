package org.scalamu.core.runners

import java.io._
import java.net.{ServerSocket, Socket}

import io.circe.{Decoder, Encoder}
import org.scalamu.core.CommunicationException

import scala.util.control.NonFatal

abstract class SocketConnectionHandler[I: Encoder, O: Decoder] {
  def socket: ServerSocket
  
  def pipeFactory(
    client: Socket,
    is: DataInputStream,
    os: DataOutputStream
  ): CommunicationPipe[I, O] = new CommunicationPipe[I, O](client, is, os)

  def handle(): Either[CommunicationException, CommunicationPipe[I, O]] =
    try {
      val client = socket.accept()
      val is     = new DataInputStream(new BufferedInputStream(client.getInputStream))
      val os     = new DataOutputStream(new BufferedOutputStream(client.getOutputStream))
      Right(pipeFactory(client, is, os))
    } catch {
      case NonFatal(e) => Left(CommunicationException(e))
    }
}
