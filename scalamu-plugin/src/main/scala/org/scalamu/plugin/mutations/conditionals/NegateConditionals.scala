package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Mutation, that replaces conditional operators with their logical counterparts.
 * e.g.
 * {{{
 * if (a > 10) {
 *   ..
 * } else if (c == 10) {
 *   ..
 * }
 * }}}
 * is replaced with
 * {{{
 * if (a <= 10) {
 *   ..
 * } else if (c != 10) {
 *   ..
 * }
 * }}}
 */
case object NegateConditionals extends Mutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._

      override def mutation: Mutation = self

      override def transformer(): Transformer = {
        case q"$lhs == $rhs" => q"$lhs != $rhs"
        case q"$lhs != $rhs" => q"$lhs == $rhs"
        case q"$lhs > $rhs"  => q"$lhs <= $rhs"
        case q"$lhs < $rhs"  => q"$lhs >= $rhs"
        case q"$lhs >= $rhs" => q"$lhs < $rhs"
        case q"$lhs <= $rhs" => q"$lhs > $rhs"
        case tree            => tree
      }
    }
}
