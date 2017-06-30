package org.scalamu.core

import java.nio.file.{Path, Paths}

import scopt.Read

import scala.util.matching.Regex

package object configuration {
  implicit val pathRead: Read[Path]   = Read.reads(Paths.get(_))
  implicit val regexRead: Read[Regex] = Read.reads(_.r)
}
