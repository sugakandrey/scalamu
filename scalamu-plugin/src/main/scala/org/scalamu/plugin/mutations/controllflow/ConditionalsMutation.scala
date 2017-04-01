package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.mutations.{NumericTypesSupport, OperatorMutation}

trait ConditionalsMutation extends OperatorMutation with NumericTypesSupport {
  protected final def comparisonOperators = Set(
    ">",
    "<",
    ">=",
    "<="
  )

  protected final def equalityCheckOperators = Set(
    "==",
    "!="
  )

  override protected def supportedOperators: Set[String] =
    comparisonOperators | equalityCheckOperators

  override protected def mutationRules: Map[String, String] = Map(
    ">"  -> "<=",
    "<"  -> ">=",
    ">=" -> "<",
    "<=" -> ">",
    "==" -> "!=",
    "!=" -> "=="
  )
}
