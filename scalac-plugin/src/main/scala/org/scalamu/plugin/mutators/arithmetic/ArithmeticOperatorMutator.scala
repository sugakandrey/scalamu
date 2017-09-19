package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.mutators.BinaryOperatorMutator

/**
 * Base trait for all arithmetic related mutation operators.
 */
trait ArithmeticOperatorMutator extends BinaryOperatorMutator {
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
