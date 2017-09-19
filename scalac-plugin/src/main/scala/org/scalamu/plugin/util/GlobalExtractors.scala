package org.scalamu.plugin.util

trait GlobalExtractors extends TypeEnrichment { self: CompilerAccess =>
  import global._
  
  def isMutationGuard(symbolName: String): Boolean

  object TreeWithType {
    def unapply(tree: Tree): Option[(Tree, Type)] =
      if (tree.tpe == null) None
      else Some((tree, tree.tpe.simplify))
  }

  object GuardedMutant {
    def unapply(tree: Tree): Option[(Tree, Tree, Tree)] = tree match {
      case If(cond @ q"$guard == $lit", thenp, elsep)
        if isMutationGuard(guard.symbol.fullName) =>
        Some((cond, thenp, elsep))
      case _ => None
    }
  }

  object MaybeTypedApply {
    def unapply(tree: Tree): Option[(Tree, TermName, List[Tree])] = tree match {
      case Apply(inner, args) =>
        inner match {
          case Select(qualifier, name: TermName)               => Some((qualifier, name, args))
          case TypeApply(Select(qualifier, name: TermName), _) => Some((qualifier, name, args))
          case _                                               => None
        }
      case _ => None
    }
  }
}
