package org.scalamu.core

import java.nio.file.{Path, Paths}

import cats.syntax.either._
import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import org.scalamu.common.MutantId
import org.scalamu.plugin.{Mutation, ScalamuPluginConfig}

import scala.util.matching.Regex

package object runners {
  def tryWith[R <: AutoCloseable, T](resource: => R)(f: R => T): Either[Throwable, T] =
    Either.catchNonFatal(resource).flatMap { resource =>
      Either.catchNonFatal(f(resource)).flatMap { result =>
        Either.catchNonFatal {
          if (resource != null) resource.close()
        }.map(_ => result)
      }
    }

  implicit val encodePath: Encoder[Path] = Encoder.encodeString.contramap(_.toString)
  implicit val decodePath: Decoder[Path] = Decoder.decodeString.emap(
    s => Either.catchNonFatal(Paths.get(s)).leftMap(err => s"Can't convert $s to path. $err")
  )

  implicit val encodeRegex: Encoder[Regex]  = Encoder.encodeString.contramap(_.regex)
  implicit val decoderRegex: Decoder[Regex] = Decoder.decodeString.map(_.r)

  implicit val encodeMutation: Encoder[Mutation] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeMutation: Decoder[Mutation] =
    Decoder.decodeString.map(ScalamuPluginConfig.mutationByName)

  implicit val keyEncoder: KeyEncoder[MutantId] = id =>
    KeyEncoder.encodeKeyInt(id.id)

  implicit val keyDecoder: KeyDecoder[MutantId] = key =>
    KeyDecoder.decodeKeyInt(key).map(MutantId)
}
