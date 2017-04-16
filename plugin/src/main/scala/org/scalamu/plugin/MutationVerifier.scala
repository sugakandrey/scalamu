package org.scalamu.plugin

import org.scalamu.plugin.util.{CompilerAccess, GlobalExtractors}

/**
 * Used to verify that given compilation unit contains no nested mutants in it.
 */
private[plugin] trait MutationVerifier extends GlobalExtractors { self: CompilerAccess =>
  import global._

  def treesWithNestedMutations(tree: Tree): Seq[Tree] = {
    val isGuardedMutant: PartialFunction[Tree, Tree] = {
      case t @ GuardedMutant(_, _, _) => t
    }
    val traverser = new CollectTreeTraverser(isGuardedMutant)
    traverser.traverse(tree)
    val allMutants = traverser.results

    allMutants.filter {
      case GuardedMutant(_, mutated, _) =>
        traverser.results.clear()
        traverser.traverse(mutated)
        traverser.results.nonEmpty
    }
  }
}
