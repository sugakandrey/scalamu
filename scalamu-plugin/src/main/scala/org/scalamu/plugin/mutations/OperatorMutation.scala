package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationReporter}

import scala.tools.nsc.Global

/**
 * Base trait for mutations, which are only applicable to syntactic trees matching
 * the following patter `Apply(Select(lhs, op @ TermName(_)) List(arg) `, where `op`
 * must be in [[org.scalamu.plugin.mutations.OperatorMutationRules.supportedOperators]],
 * and is replaced according to [[org.scalamu.plugin.mutations.OperatorMutationRules.mutationRules]]
 */
trait OperatorMutation extends Mutation with OperatorMutationRules { self: SupportedTypes =>
  override def mutatingTransformer(
    global: Global,
    mutationReporter: MutationReporter
  ): MutatingTransformer = new MutatingTransformer(global, mutationReporter) {
    import global._

    override protected def mutation: Mutation = self

    private def isAppropriateType(tpe: Type): Boolean = supportedTypes(global).exists(_ =:= tpe)

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
          val mutatedLhs     = super.transform(lhs)
          val mutatedRhs     = super.transform(rhs)
          val mutationResult = q"$mutatedLhs.$mutatedOp(..$mutatedRhs)"
          reportMutation(tree, mutationResult)
          mutationGuard(mutationResult, tree)
      }
    }
  }
}
