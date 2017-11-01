package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import org.scalamu.core.api.CommunicationException

class ProcessCommunicationHandler[I: Encoder, O: Decoder](
  override val socket: ServerSocket,
  initialize: DataOutputStream => Unit
) extends SocketConnectionHandler[I, O] {

  override def handle(): Either[CommunicationException, CommunicationPipe[I, O]] = {
    val tryOpenPipe = super.handle()
    for {
      pipe <- tryOpenPipe
      _    <- Either.catchNonFatal(initialize(pipe.os)).leftMap(CommunicationException)
    } yield pipe
  }
}
