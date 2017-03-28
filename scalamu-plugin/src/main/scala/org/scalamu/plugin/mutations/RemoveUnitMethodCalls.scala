package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Mutation, that removes calls to methods with [[Unit]] return type.
 * e.g.
 * {{{
 * val a = 123
 * println(a)
 * }}}
 * is replaced with
 * {{{
 * val a = 123
 * ()
 * }}}
 */
object RemoveUnitMethodCalls extends Mutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._
      import definitions.UnitTpe

      override def mutation: Mutation = self

      private def returnsUnit(tpe: Type): Boolean = tpe match {
        case mtpe: MethodType => mtpe.resultType =:= UnitTpe
        case ptpe: PolyType   => ptpe.resultType =:= UnitTpe
        case _                => false
      }

      override val transformer: Transformer = {
        case Apply(method @ Select(term, TermName(_)), _) if returnsUnit(method.tpe) => q"$term"
        case q"$fn(..$args)" if returnsUnit(fn.tpe)                                  => q"()"
        case tree                                                                    => tree
      }
    }
}
