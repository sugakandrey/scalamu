package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.mutators.{BinaryOperatorMutator, NumericTypesSupport}

trait ConditionalsMutator extends BinaryOperatorMutator with NumericTypesSupport {

  override protected def mutationRules: Map[String, String] = Map(
    ">"  -> "<=",
    "<"  -> ">=",
    ">=" -> "<",
    "<=" -> ">",
    "==" -> "!=",
    "!=" -> "=="
  )
}
