package org.scalamu.plugin.mutations

import org.scalamu.plugin.ScalamuConfig.mutationGuardPrefix

import scala.tools.nsc.Global

private[plugin] class GlobalUtils(val global: Global) {
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

  object GuardedMutation {
    def unapply(tree: Tree): Option[(TermName, Tree, Tree)] = tree match {
      case q"if (${guard: TermName}) $thenp else $elsep"
          if guard.containsName(mutationGuardPrefix) =>
        Some((guard, thenp, elsep))
      case _ => None
    }
  }
}
