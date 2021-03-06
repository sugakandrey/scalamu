package org.scalamu.plugin.util

trait GlobalExtractors extends TypeEnrichment { self: CompilerAccess =>
  import global._

  def isMutationGuard(symbolName: String): Boolean

  object TreeWithType {
    def unapply(tree: Tree): Option[(Tree, Type)] =
      if (tree.tpe == null) None
      else Some((tree, tree.tpe.simplify))
  }

  object ScoverageInstrumentedStatement {
    private[this] val fqn = "org.scalamu.compilation.ForgetfulInvoker.invoked"

    def unapply(tree: Tree): Option[Tree] = tree match {
      case Block(List(instrumented), original) if Option(instrumented.symbol).exists(_.fullName == fqn) =>
        Some(original)
      case _ => None
    }
  }

  object GuardedMutant {
    def unapply(tree: Tree): Option[(Tree, Tree, Tree)] = tree match {
      case If(cond @ q"$guard == $lit", thenp, elsep)
          if Option(guard.symbol).exists(s => isMutationGuard(s.fullName)) =>
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
