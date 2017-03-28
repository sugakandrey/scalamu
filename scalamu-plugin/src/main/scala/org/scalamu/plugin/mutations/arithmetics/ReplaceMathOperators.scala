package org.scalamu.plugin.mutations.arithmetics

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Mutation, that replaces integer and floating point arithmetic operators with
 * their counterparts using a set of predefined rules.
 * e.g.
 * {{{
 * val a = 123
 * val b = a - 1
 * val c = foo(a) * b
 * }}}
 * is mutated to
 * {{{
 * val a = 123
 * val b = a + 1
 * val c = foo(a) / b
 * }}}
 */
object ReplaceMathOperators extends ArithmeticOperatorMutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global
      import context.global._

      override protected def mutation: Mutation = self

      override protected val transformer: Transformer = {
        case Apply(Select(lhs, op), List(arg))
            if supportedOperators.contains(op.decodedName.toString)
              && isAppropriatelyTyped(global)(lhs)
              && isAppropriatelyTyped(global)(arg) =>
          val replacement = mutationRules(op.decodedName.toString)
          q"$lhs.${encode(replacement)}(..$arg)"
        case tree => tree
      }
    }
}
