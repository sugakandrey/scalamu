package org.scalamu.plugin

import org.scalamu.plugin.util.{CompilerAccess, GlobalExtractors}

/**
 * Used to verify that given compilation unit contains no nested mutations in it.
 */
private[plugin] trait MutationVerifier extends GlobalExtractors { self: CompilerAccess =>
  import global._

  def treesWithNestedMutations(tree: Tree): Seq[Tree] = {
    val isMutation: PartialFunction[Tree, Tree] = {
      case t @ GuardedMutation(_, _, _) => t
    }
    val traverser = new CollectTreeTraverser(isMutation)
    traverser.traverse(tree)
    val allMutations = traverser.results
    println(allMutations.size)

    allMutations.filter {
      case GuardedMutation(_, mutated, _) =>
        traverser.results.clear()
        traverser.traverse(mutated)
        traverser.results.nonEmpty
    }
  }
}
