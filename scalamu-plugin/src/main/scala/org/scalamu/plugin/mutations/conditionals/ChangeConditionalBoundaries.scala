package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Mutation, that replaces boundaries on conditional operators { <, >, <=, >= }
 * with their counterparts.
 * e.g.
 * {{{
 * if (foo > bar) {
 *   ...
 * }
 * }}}
 * is mutated to
 * {{{
 * if (foo <= bar) {
 *   ...
 * }
 * }}}
 *
 */
case object ChangeConditionalBoundaries extends Mutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._

      override protected def mutation: Mutation = self

      override protected def transformer(): Transformer = {
        case q"$lhs < $rhs"  => q"$lhs <= $rhs"
        case q"$lhs > $rhs"  => q"$lhs >= $rhs"
        case q"$lhs <= $rhs" => q"$lhs < $rhs"
        case q"$lhs >= $rhs" => q"$lhs > $rhs"
        case tree            => tree
      }
    }
}
