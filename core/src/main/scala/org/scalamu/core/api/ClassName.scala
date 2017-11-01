package org.scalamu.core.api

import io.circe.{Decoder, Encoder}
import org.scalamu.core.utils.ClassLoadingUtils

import scala.util.Try

final case class ClassName(fullName: String) {
  require(fullName.nonEmpty)

  private lazy val segments = fullName.split(".")

  def name: String             = segments.last
  def packageName: Seq[String] = segments.init

  def loadFromContextClassLoader: Try[Class[_]] =
    Try(
      Class.forName(
        fullName,
        true,
        ClassLoadingUtils.contextClassLoader
      )
    )
}

object ClassName {
  def fromDescriptor(descriptor: String): ClassName =
    ClassName.fromInternal(descriptor.substring(1, descriptor.length - 1))

  def fromInternal(name: String): ClassName = ClassName(name.replaceAll("/", "."))
  def forClass(aClass: Class[_]): ClassName = ClassName(aClass.getName)

  implicit val encodeClassName: Encoder[ClassName] = Encoder.encodeString.contramap(_.fullName)
  implicit val decodeClassName: Decoder[ClassName] = Decoder.decodeString.map(ClassName(_))
}
