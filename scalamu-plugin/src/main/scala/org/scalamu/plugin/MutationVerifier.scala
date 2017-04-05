package org.scalamu.plugin

import org.scalamu.plugin.mutations.{CompilerAccess, GlobalExtractors, TypeEnrichment}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Used to verify that given compilation unit contains no nested mutations in it.
 */
private[plugin] trait MutationVerifier {
  self: CompilerAccess with TypeEnrichment with GlobalExtractors =>
  import global._

  private[this] class MutationCollector extends Traverser {
    val results: ListBuffer[Tree] = mutable.ListBuffer.empty[Tree]

    override def traverse(tree: Tree): Unit = tree match {
      case GuardedMutation(_, _, _) => results += tree
      case _                        => super.traverse(tree)
    }
  }

  def hasNoNestedMutations(tree: Tree, insideMutation: Boolean = false): Boolean = {
    val mutations: mutable.ListBuffer[Tree] = new MutationCollector().results
    mutations match {
      case ms if ms.isEmpty     => true
      case ms if insideMutation => false
      case ms                   => ms.forall(hasNoNestedMutations(_, insideMutation = true))
    }
  }
}
