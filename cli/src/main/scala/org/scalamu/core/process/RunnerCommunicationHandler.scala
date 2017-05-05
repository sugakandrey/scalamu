package org.scalamu.core.process

import java.io.{DataInputStream, DataOutputStream}
import java.net.ServerSocket

import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import io.circe.Decoder
import io.circe.parser._

class RunnerCommunicationHandler[R: Decoder](
  override val socket: ServerSocket,
  override val initialize: DataOutputStream => Unit
) extends SocketConnectionHandler[R] {

  override def receive(is: DataInputStream): Either[Throwable, List[R]] =
    Iterator
      .continually(Either.catchNonFatal(is.readUTF()))
      .takeWhile(_.isRight)
      .map(_.flatMap(decode[R]))
      .toList
      .sequenceU
}
