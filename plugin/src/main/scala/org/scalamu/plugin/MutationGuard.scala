package org.scalamu.plugin

import scala.tools.nsc.Global

trait MutationGuard {
  def apply(global: Global)(mutated: global.Tree, untouched: global.Tree): global.Tree
  
  def isGuardSymbol(symbolName: String): Boolean
}

private[plugin] case object NoOpGuard extends MutationGuard {
  override def apply(global: Global)(mutated: global.Tree, untouched: global.Tree): global.Tree =
    mutated

  override def isGuardSymbol(symbolName: String): Boolean = false
}

final case class FqnGuard(
  fqn: String
) extends MutationGuard {
  private var currentSourceName: String = _
  private var currentMutationId: Int    = 1

  override def apply(global: Global)(mutated: global.Tree, untouched: global.Tree): global.Tree = {
    import global._
    val sourceName = global.currentSource.path
    if (sourceName == currentSourceName) {
      currentMutationId += 1
    } else {
      currentSourceName = sourceName
      currentMutationId = 1
    }
    val guardTerm = findMemberFromRoot(TermName(fqn)).asTerm
    val guard =
      q"$guardTerm(${Literal(Constant(currentSourceName))}) == ${Literal(Constant(currentMutationId))}"
    q"(if ($guard) $mutated else $untouched)"
  }

  override def isGuardSymbol(symbolName: String): Boolean = symbolName == fqn
}
