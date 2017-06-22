package org.scalamu.core.runners

import java.io._
import java.net.ServerSocket

import cats.syntax.either._
import org.scalamu.core.CommunicationException

import scala.util.control.NonFatal

trait SocketConnectionHandler[R] {
  def socket: ServerSocket
  def initialize: DataOutputStream => Unit
  def communicate(is: DataInputStream, os: DataOutputStream): Either[Throwable, List[R]]

  def handle(): Either[CommunicationException, List[R]] = {
    val client = socket.accept()
    try {
      val is = new DataInputStream(new BufferedInputStream(client.getInputStream))
      val os = new DataOutputStream(new BufferedOutputStream(client.getOutputStream))
      initialize(os)
      val data = communicate(is, os)
      data.leftMap(CommunicationException)
    } catch {
      case NonFatal(e) => Left(CommunicationException(e))
    } finally {
      client.close()
    }
  }
}
