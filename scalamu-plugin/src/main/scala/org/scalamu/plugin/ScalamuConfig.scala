package org.scalamu.plugin

import org.scalamu.plugin.mutations.{ReplaceWithNil, ReplaceWithNone}
import org.scalamu.plugin.mutations.controllflow._
import org.scalamu.plugin.mutations.arithmetic._
import org.scalamu.plugin.mutations.methodcalls._

object ScalamuConfig {

  /**
   * The order of mutations is important, in some cases wrong order can lead to nested mutations
    * (unreachable code) being generated.
    * e.g. if [[ReplaceMathOperators]] is applied after [[InvertNegations]] in x * -2
    * {{{
    * if (mu2) {
    *   x / (if (mu1) 2 else -2)
    * } else {
    *   x * (if (mu1) 2 else -2)
    * }
    * }}}
    * instead of
    * {{{
    * if (mu1) {
    *   x / -2
    * } else {
    *   x * (if (mu2) 2 else -2)
    * }
    * }}}
   */
  val allMutations: Seq[Mutation] = Seq(
    ReplaceCaseWithWildcard,
    ReplaceMathOperators,
    ReplaceWithIdentityFunction,
    InvertNegations,
    AlwaysExecuteConditionals,
    NeverExecuteConditionals,
    ChangeConditionalBoundaries,
    NegateConditionals,
    RemoveUnitMethodCalls,
    ChangeRangeBoundary,
    ReplaceLogicalOperators,
    ReplaceWithNone,
    ReplaceWithNil
  )

  val mutationGuardPrefix: String = "org.scalamu.guards"
}
