package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.mutations.NumericTypesSupport

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
case object ReplaceMathOperators extends ArithmeticOperatorMutation with NumericTypesSupport {
  override def description: String = "Replaced math operator"
}
