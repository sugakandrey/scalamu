package org.scalamu.plugin.mutations

import org.scalamu.plugin.ScalamuConfig.mutationGuardPrefix

trait GlobalExtractors { self: CompilerAccess with TypeEnrichment =>
  import global._
  
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
