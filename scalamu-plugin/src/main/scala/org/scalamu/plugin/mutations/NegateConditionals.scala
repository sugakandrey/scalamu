package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
  * Created by sugakandrey.
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
