package org.scalamu.plugin.mutations

import scala.tools.nsc.Global

class TypeEnrichment(val global: Global) {
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

  object TreeWithType {
    def unapply(tree: Tree): Option[(Tree, Type)] =
      if (tree.tpe == null) None
      else Some((tree, tree.tpe.simplify))
  }
}
