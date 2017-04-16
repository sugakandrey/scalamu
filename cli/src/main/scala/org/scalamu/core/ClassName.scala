package org.scalamu.core

final case class ClassName(className: String) {
  require(className.nonEmpty)
  
  private lazy val segments = className.split(".")
  
  def name: String = segments.last
  def packageName: Seq[String] = segments.init
}

object ClassName {
  def fromDescriptor(descriptor: String): ClassName =
    ClassName.fromInternalName(descriptor.substring(1, descriptor.length - 1))
  def fromInternalName(name: String): ClassName = ClassName(name.replaceAll("/", "."))
}
