package org.scalamu.plugin

import org.scalamu.plugin.mutators.arithmetic._
import org.scalamu.plugin.mutators.controllflow._
import org.scalamu.plugin.mutators.methodcalls._
import org.scalamu.plugin.mutators.{ReplaceWithNil, ReplaceWithNone}

object ScalamuPluginConfig {
  val allMutators: Seq[Mutator] = Seq(
    ReplaceCaseWithWildcard,
    ReplaceMathOperators,
    ReplaceWithIdentityFunction,
    InvertNegations,
    AlwaysExecuteConditionals,
    NeverExecuteConditionals,
    ReplaceConditionalBoundaries,
    NegateConditionals,
    RemoveUnitMethodCalls,
    ChangeRangeBoundary,
    ReplaceLogicalOperators,
    ReplaceWithNone,
    ReplaceWithNil
  )

  val mutatorsByName: Map[String, Mutator] =
    allMutators.map(m => m -> m.toString).toMap.map(_.swap)

  val mutationGuardPrefix: String = "org.scalamu.guards"
}
