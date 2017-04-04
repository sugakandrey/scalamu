package org.scalamu.plugin.mutations

trait TypeEnrichment { self: CompilerAccess =>
  import global._

  implicit class RichType(tpe: Type) {
    def ~(other: Type): Boolean = isEquivalentTo(other)

    def isEquivalentTo(other: Type): Boolean =
      if (tpe == null || other == null) false
      else tpe.simplify =:= other.simplify

    def simplify: Type =
      if (tpe != null) tpe.dealiasWiden.deconst
      else null
  }
}
