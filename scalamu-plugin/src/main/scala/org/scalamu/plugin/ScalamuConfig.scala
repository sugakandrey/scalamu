package org.scalamu.plugin

import org.scalamu.plugin.mutations.conditionals._
import org.scalamu.plugin.mutations.arithmetics._
import org.scalamu.plugin.mutations.methodcalls._

object ScalamuConfig {
  val allMutations: Seq[Mutation] = Seq(
    InvertNegations,
    ReplaceMathOperators,
    AlwaysExecuteConditionals,
    NeverExecuteConditionals,
    NegateConditionals,
    ChangeConditionalBoundaries, 
    NegateConditionals,
    RemoveUnitMethodCalls,
    ReplaceWithIdentityFunction
  )
}
