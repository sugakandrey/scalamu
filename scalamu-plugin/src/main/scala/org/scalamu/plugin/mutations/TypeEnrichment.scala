package org.scalamu.plugin.mutations

import scala.tools.nsc.Global

class TypeEnrichment(val global: Global) {
  import global._

  implicit class RichType(tpe: Type) {
    def ~(other: Type): Boolean              = isEquivalentTo(other)
    def isEquivalentTo(other: Type): Boolean = tpe.simplify =:= other.simplify
    def simplify: Type                       = tpe.dealiasWiden.deconst
  }
}
