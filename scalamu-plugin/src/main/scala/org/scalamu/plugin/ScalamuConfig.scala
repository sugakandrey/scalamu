package org.scalamu.plugin

import org.scalamu.plugin.mutations.controllflow._
import org.scalamu.plugin.mutations.arithmetic._
import org.scalamu.plugin.mutations.methodcalls._

object ScalamuConfig {
  val allMutations: Seq[Mutation] = Seq(
    InvertNegations,
    ReplaceMathOperators,
    AlwaysExecuteConditionals,
    NeverExecuteConditionals,
    ChangeConditionalBoundaries, 
    NegateConditionals,
    RemoveUnitMethodCalls,
    ReplaceWithIdentityFunction,
    ChangeRangeBoundary,
    ReplaceLogicalOperators
  )
  
  val mutationGuardPrefix: String = "org.scalamu.guards"
}
