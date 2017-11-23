package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}
import org.scalamu.plugin.mutators.{AbstractBinaryOperatorMutator, NumericTypesSupport}

import scala.tools.nsc.Global

/**
 * Mutation operator that replaces integer and floating point arithmetic operators with
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
case object ReplaceMathOperators extends AbstractBinaryOperatorMutator {
  override def description: String = "Replaced math operator."

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new BinaryOperatorTransformer(config)(global) with NumericTypesSupport {
      override protected def operatorNameMapping: Map[String, String] = Map(
        "+"  -> "-",
        "-"  -> "+",
        "/"  -> "*",
        "%"  -> "*",
        "*"  -> "/",
        "&"  -> "|",
        "|"  -> "&",
        "^"  -> "&",
        "<<" -> ">>",
        ">>" -> "<<"
      )
    }
}
