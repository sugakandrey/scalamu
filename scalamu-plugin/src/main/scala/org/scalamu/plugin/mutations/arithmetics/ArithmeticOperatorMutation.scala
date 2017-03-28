package org.scalamu.plugin.mutations.arithmetics

import org.scalamu.plugin.mutations.{NumericTypesSupport, OperatorMutation}

/**
 * Base trait for all arithmetic related mutations.
 */
trait ArithmeticOperatorMutation extends OperatorMutation with NumericTypesSupport {
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
