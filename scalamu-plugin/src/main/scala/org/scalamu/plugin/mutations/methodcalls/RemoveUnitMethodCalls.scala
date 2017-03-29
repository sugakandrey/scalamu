package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.mutations.SupportedTypes
import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

import scala.tools.nsc.Global

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
object RemoveUnitMethodCalls extends Mutation with SupportedTypes { self =>
  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(global.definitions.UnitTpe)

  override protected def isAppropriatelyTyped(global: Global)(tree: global.Tree): Boolean =
    tree.tpe != null && tree.tpe =:= global.definitions.UnitTpe

  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global._
      import context.global

      override def mutation: Mutation = self

      override val transformer: Transformer = {
        case t @ q"$fn(..$args)" if isAppropriatelyTyped(global)(t)      => q"()"
        case q"(..$args) => $expr" if isAppropriatelyTyped(global)(expr) => q"(..$args) => ()"
        case tree                                                        => tree
      }
    }
}
