package org.scalamu.plugin

import org.scalamu.common.MutationId

import scala.tools.nsc.Global

trait MutationGuard {
  def apply(
    global: Global
  )(mutated: global.Tree, untouched: global.Tree, id: MutationId): global.Tree

  def isGuardSymbol(symbolName: String): Boolean
}

private[plugin] case object NoOpGuard extends MutationGuard {
  override def apply(
    global: Global
  )(mutated: global.Tree, untouched: global.Tree, id: MutationId): global.Tree =
    mutated

  override def isGuardSymbol(symbolName: String): Boolean = false
}

final case class FqnGuard(
  fqn: String
) extends MutationGuard {

  override def apply(
    global: Global
  )(mutated: global.Tree, untouched: global.Tree, id: MutationId): global.Tree = {
    import global._
    val guardTerm = findMemberFromRoot(TermName(fqn))
    val guard     = q"$guardTerm == ${Literal(Constant(id.id))}"
    q"if ($guard) $mutated else $untouched"
  }

  override def isGuardSymbol(symbolName: String): Boolean = symbolName == fqn
}
