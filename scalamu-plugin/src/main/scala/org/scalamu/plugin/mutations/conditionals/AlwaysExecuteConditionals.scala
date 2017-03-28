package org.scalamu.plugin.mutations.conditionals

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Mutation, that guarantees that conditional blocks always execute.
 * e.g.
 * {{{
 * if (cond()) {
 *   foo()
 * } else {
 *   bar()
 * }
 * }}}
 * is mutated to
 * {{{
 * foo()
 * }}}
 */
case object AlwaysExecuteConditionals extends ConditionalsMutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._

      override protected def mutation: Mutation = self

      override protected def transformer: Transformer = {
        case q"if ($cond) $thenp else $elsep" =>
          q"$thenp"
        case tree => tree
      }
    }
}
