package org.scalamu.plugin

import org.scalamu.plugin.util.{CompilerAccess, GlobalExtractors}

import scala.collection.mutable.ListBuffer
import scala.collection.{breakOut, mutable}

/**
 * Used to verify that given compilation unit contains no nested mutations in it.
 */
private[plugin] trait MutationVerifier extends GlobalExtractors {
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
