package org.scalamu.plugin

import org.scalamu.plugin.mutations.{CompilerAccess, GlobalExtractors, TypeEnrichment}

import scala.collection.{breakOut, mutable}
import scala.collection.mutable.ListBuffer

/**
 * Used to verify that given compilation unit contains no nested mutations in it.
 */
private[plugin] trait MutationVerifier extends TypeEnrichment with GlobalExtractors {
  self: CompilerAccess =>
  import global._

  private[this] class MutationCollector extends Traverser {
    val results: ListBuffer[Tree] = mutable.ListBuffer.empty[Tree]

    override def traverse(tree: Tree): Unit = tree match {
      case GuardedMutation(_, _, _) => results += tree
      case _                        => super.traverse(tree)
    }
  }

  def treesWithNestedMutations(tree: Tree, insideMutation: Boolean = false): List[Tree] = {
    val collector = new MutationCollector()
    collector.traverse(tree)
    val mutations = collector.results
    mutations match {
      case ms if ms.isEmpty     => Nil
      case ms if insideMutation => List(tree)
      case ms =>
        ms.flatMap {
          case GuardedMutation(_, mutated, _) =>
            treesWithNestedMutations(mutated, insideMutation = true)
        }(breakOut)
    }
  }
}
