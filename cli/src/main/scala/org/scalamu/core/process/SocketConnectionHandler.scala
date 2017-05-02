package org.scalamu.core.process

import java.io._
import java.net.ServerSocket

import cats.syntax.either._
import org.scalamu.core.CommunicationException

import scala.util.control.NonFatal

trait SocketConnectionHandler[R] {
  def socket: ServerSocket
  def initialize: DataOutputStream => Unit
  def receive(is: DataInputStream): Either[Throwable, Seq[R]]

  def handle(): Either[CommunicationException, Seq[R]] = {
    val client = socket.accept()
    try {
      val is = new DataInputStream(client.getInputStream)
      initialize(new DataOutputStream(client.getOutputStream))
      val data = receive(is)
      is.close()
      data.leftMap(CommunicationException)
    } catch {
      case NonFatal(e) => Left(CommunicationException(e))
    } finally {
      client.close()
    }
  }
}
