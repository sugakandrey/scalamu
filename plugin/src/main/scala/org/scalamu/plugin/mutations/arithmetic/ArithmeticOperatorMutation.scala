package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.mutations.BinaryOperatorMutation

/**
 * Base trait for all arithmetic related mutations.
 */
trait ArithmeticOperatorMutation extends BinaryOperatorMutation {
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
