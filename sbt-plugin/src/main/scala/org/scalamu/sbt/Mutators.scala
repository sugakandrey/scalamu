package org.scalamu.sbt

object Mutators {
  sealed trait Mutator
  case object ReplaceCaseWithWildcard      extends Mutator
  case object ReplaceMathOperators         extends Mutator
  case object ReplaceWithIdentityFunction  extends Mutator
  case object InvertNegations              extends Mutator
  case object AlwaysExecuteConditionals    extends Mutator
  case object NeverExecuteConditionals     extends Mutator
  case object ChangeConditionalBoundaries  extends Mutator
  case object NegateConditionals           extends Mutator
  case object RemoveUnitMethodCalls        extends Mutator
  case object ChangeRangeBoundary          extends Mutator
  case object ReplaceLogicalOperators      extends Mutator
  case object ReplaceWithNone              extends Mutator
  case object ReplaceWithNil               extends Mutator
  case object ReplaceBooleanLiterals       extends Mutator
  case object ReplaceIntegerLiterals       extends Mutator
  case object ReplaceLongLiterals          extends Mutator
  case object ReplaceFloatingPointLiterals extends Mutator
  
  val enabledByDefault: Seq[Mutator] = Seq(
    ReplaceCaseWithWildcard,
    ReplaceMathOperators,
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
  
  val allMutators: Seq[Mutator] = Seq(
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
    ReplaceWithNil,
    ReplaceBooleanLiterals,
    ReplaceIntegerLiterals,
    ReplaceLongLiterals,
    ReplaceFloatingPointLiterals
  )
}
