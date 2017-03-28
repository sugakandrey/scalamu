package org.scalamu.plugin.mutations.arithmetics

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationContext}

/**
 * Mutation, thar inverts negations of integer and floating point numbers.
 * e.g.
 * {{{
 * val a = -b
 * }}}
 * is mutated to
 * {{{
 * val a = b
 * }}}
 */
object InvertNegations extends ArithmeticOperatorMutation { self =>
  override def mutatingTransformer(context: MutationContext): MutatingTransformer =
    new MutatingTransformer(context) {
      import context.global
      import context.global._

      override protected def mutation: Mutation = self

      override protected val transformer: Transformer = {
        case q"${lit: Constant}" if lit.isNumeric && lit.doubleValue < 0 =>
          val value: Any = lit.value match {
            case v: Byte =>
              if (v == Byte.MinValue) Byte.MaxValue
              else -lit.byteValue
            case v: Short =>
              if (v == Short.MinValue) Short.MaxValue
              else -lit.shortValue
            case v: Int =>
              if (v == Int.MinValue) Int.MaxValue
              else -lit.intValue
            case v: Long =>
              if (v == Long.MinValue) Long.MaxValue
              else -lit.longValue
            case _: Float  => -lit.doubleValue
            case _: Double => -lit.floatValue
          }
          Literal(Constant(value))
        case q"-$term" if isAppropriatelyTyped(global)(term) => q"$term"
        case tree                                            => tree
      }
    }
}
