package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.mutations.{BinaryOperatorMutation, NumericTypesSupport}

/**
 * Base trait for all arithmetic related mutations.
 */
trait ArithmeticOperatorMutation extends BinaryOperatorMutation with NumericTypesSupport {
  override protected val supportedOperators = Set(
    "+",
    "-",
    "%",
    "/",
    "*",
    "|",
    "&",
    "^",
    "<<",
    ">>"
  )

  override protected val mutationRules = Map(
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
