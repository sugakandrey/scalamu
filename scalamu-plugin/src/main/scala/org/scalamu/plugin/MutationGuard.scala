package org.scalamu.plugin

import scala.collection.mutable
import scala.reflect.io.Path
import scala.tools.nsc.Global

sealed trait MutationGuard {
  def apply(global: Global)(mutated: global.Tree, untouched: global.Tree): global.Tree
}

private[plugin] case object NoOpGuard extends MutationGuard {
  override def apply(global: Global)(mutated: global.Tree, untouched: global.Tree): global.Tree =
    mutated
}

private[plugin] final case class FqnPrefixedGuard(
  guardPackagePrefix: String
) extends MutationGuard {
  private val currentMutationId = mutable.Map.empty[String, Int].withDefaultValue(1)

  override def apply(global: Global)(mutated: global.Tree, untouched: global.Tree): global.Tree = {
    import global._
    val fileName   = global.currentSource.file.name
    val extension  = Path.extension(fileName)
    val sourceName = fileName.substring(0, fileName.length - extension.length - 1)
    val mutationId = currentMutationId(sourceName)
    val guardTerm = findMemberFromRoot(
      TermName(s"$guardPackagePrefix.${sourceName}Guard.enabledMutation")
    ).asTerm
    val guard = q"$guardTerm == ${Literal(Constant(mutationId))}"
    currentMutationId(sourceName) += 1
    q"(if ($guard) $mutated else $untouched)"
  }
}
