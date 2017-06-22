package org.scalamu.core.runners

import java.io.{DataInputStream, DataOutputStream}
import java.net.ServerSocket

import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import io.circe.Decoder
import io.circe.parser._

class WorkerCommunicationHandler[R: Decoder](
  override val socket: ServerSocket,
  override val initialize: DataOutputStream => Unit
) extends SocketConnectionHandler[R] {

  protected def exchangeData(is: DataInputStream, os: DataOutputStream): Either[Throwable, String] =
    Either.catchNonFatal(is.readUTF())

  override def communicate(is: DataInputStream, os: DataOutputStream): Either[Throwable, List[R]] =
    Iterator
      .continually(exchangeData(is, os))
      .takeWhile(_.isRight)
      .map(_.flatMap(decode[R]))
      .toList
      .sequenceU
}
