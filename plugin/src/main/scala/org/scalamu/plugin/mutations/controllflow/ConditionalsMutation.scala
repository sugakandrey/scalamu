package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.mutations.{BinaryOperatorMutation, NumericTypesSupport}

trait ConditionalsMutation extends BinaryOperatorMutation with NumericTypesSupport {

  override protected def mutationRules: Map[String, String] = Map(
    ">"  -> "<=",
    "<"  -> ">=",
    ">=" -> "<",
    "<=" -> ">",
    "==" -> "!=",
    "!=" -> "=="
  )
}
