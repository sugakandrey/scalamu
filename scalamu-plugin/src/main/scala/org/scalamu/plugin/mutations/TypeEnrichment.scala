package org.scalamu.plugin.mutations

trait TypeEnrichment { self: CompilerAccess =>
  import global._

  implicit class RichType(tpe: Type) {
    def ~(other: Type): Boolean = conformsTo(other, _ =:= _)

    def <~<(other: Type): Boolean = conformsTo(other, _ <:< _)

    def conformsTo(other: Type, op: (Type, Type) => Boolean): Boolean =
      if (tpe == null || other == null) false
      else op(tpe.simplify, other.simplify)

    def simplify: Type =
      if (tpe != null) tpe.dealiasWiden.deconst
      else null
  }
}
