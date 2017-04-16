package org.scalamu.plugin

import org.scalamu.plugin.mutations.arithmetic._
import org.scalamu.plugin.mutations.controllflow._
import org.scalamu.plugin.mutations.methodcalls._
import org.scalamu.plugin.mutations.{ReplaceWithNil, ReplaceWithNone}

object ScalamuConfig {

  /**
   * The order of mutations is important, although it is impossible to completely avoid generating
   * nested mutants (essentially dead code) just by changing it,
   * it is beneficial (where possible) to rearrange them, so that
   * `∀ i,j i > j, ∀ t1 ∈ domain(mutations(j)), t2 ∈ domain(mutations(i)): t1 isNotSubtree t2`
   *
   * e.g. [[InvertNegations]] being applied after [[ReplaceMathOperators]] will avoid generating nested mutants,
   * but the same is impossible for [[ReplaceMathOperators]] and [[ReplaceConditionalBoundaries]], since
   * both of them can be applied to trees in each other's domain.
   */
  val allMutations: Seq[Mutation] = Seq(
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

  val mutationGuardPrefix: String = "org.scalamu.guards"
}
