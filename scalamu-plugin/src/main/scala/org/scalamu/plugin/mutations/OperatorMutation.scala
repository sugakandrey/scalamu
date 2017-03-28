package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Base trait for mutations, which are only applicable to syntactic trees matching
 * the following patter `Apply(Select(lhs, op @ TermName(_)) List(arg) `, where `op`
 * must be in [[org.scalamu.plugin.mutations.OperatorMutationRules.supportedOperators]],
 * and is replaced according to [[org.scalamu.plugin.mutations.OperatorMutationRules.mutationRules]]
 */
trait OperatorMutation extends Mutation with OperatorMutationRules { self: SupportedTypes =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._
      import context.global

      override protected def mutation: Mutation = self

      override protected def transformer: Transformer = {
        case Apply(Select(lhs, op @ TermName(_)), List(arg))
            if supportedOperators.contains(op.decodedName.toString)
              && isAppropriatelyTyped(global)(lhs)
              && isAppropriatelyTyped(global)(arg) =>
          val replacement = mutationRules(op.decodedName.toString)
          q"$lhs.${encode(replacement)}(..$arg)"
        case tree => tree
      }
    }
}
