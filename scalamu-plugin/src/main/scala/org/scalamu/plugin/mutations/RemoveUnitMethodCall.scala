package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
  * Created by sugakandrey.
  */
object RemoveUnitMethodCall extends Mutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._

      override def mutation: Mutation = self

      override def transformer(): Transformer = {
        case t @ Apply(fn, _) if t.tpe =:= typeOf[Unit] => q"()"
        case tree                                       => tree
      }
    }
}
