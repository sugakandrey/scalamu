package org.scalamu.core

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
        Thread.currentThread().getContextClassLoader
      )
    )
}

object ClassName {
  def fromDescriptor(descriptor: String): ClassName =
    ClassName.fromInternal(descriptor.substring(1, descriptor.length - 1))

  def fromInternal(name: String): ClassName = ClassName(name.replaceAll("/", "."))
  def forClass(aClass: Class[_]): ClassName     = ClassName(aClass.getName)
}
