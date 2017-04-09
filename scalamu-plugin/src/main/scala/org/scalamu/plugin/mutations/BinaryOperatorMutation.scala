package org.scalamu.plugin.mutations

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Base trait for mutations, which are only applicable to syntactic trees matching
 * the following patter `Apply(Select(lhs, op @ TermName(_)) List(arg) `, where `op`
 * must be in [[org.scalamu.plugin.mutations.OperatorMutationRules.supportedOperators]],
 * and is replaced according to [[org.scalamu.plugin.mutations.OperatorMutationRules.mutationRules]]
 */
trait BinaryOperatorMutation extends Mutation with OperatorMutationRules { self: SupportedTypes =>
  override def mutatingTransformer(
    global: Global,
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutation: Mutation = self

    private def isAppropriateType(tpe: Type): Boolean = supportedTypes.exists(_ =:= tpe)

    override protected def transformer: Transformer = new Transformer {
      override val mutate: PartialFunction[Tree, Tree] = {
        case tree @ Apply(
              Select(TreeWithType(lhs, lhsTpe), op @ TermName(_)),
              List(TreeWithType(rhs, rhsTpe))
            )
            if supportedOperators.contains(op.decodedName.toString)
              && isAppropriateType(lhsTpe)
              && isAppropriateType(rhsTpe) =>
          val mutatedOp      = encode(mutationRules(op.decodedName.toString))
          val mutationResult = q"$lhs.$mutatedOp(..$rhs)"
          reportMutation(tree, mutationResult)
          val mutatedLhs = super.transform(lhs)
          val mutatedRhs = super.transform(rhs)
          guard(mutationResult, q"$mutatedLhs.$op(..$mutatedRhs)")
      }
    }
  }
}
