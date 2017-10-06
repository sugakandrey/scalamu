package org.scalamu.report

import scala.io.Source
import scala.tools.nsc.interpreter.InputStream

final case class CSSResource private (is: InputStream) extends AnyVal {
  def render: String = Source.fromInputStream(is).mkString
}

object CSSResource {
  def apply(path: String): Option[CSSResource] =
    if (!path.endsWith(".css")) None
    else {
      val resource = Option(getClass.getClassLoader.getResourceAsStream(path))
      resource.map(CSSResource.apply)
    }

}
