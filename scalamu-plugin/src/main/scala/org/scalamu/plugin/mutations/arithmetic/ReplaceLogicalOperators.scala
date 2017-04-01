package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.mutations.{OperatorMutation, SupportedTypes}

import scala.tools.nsc.Global

/**
 * Mutation, that swaps logical operators `&&` and `||`.
 */
case object ReplaceLogicalOperators extends OperatorMutation with SupportedTypes {
  override protected def supportedTypes(implicit global: Global): Seq[global.Type] = {
    import global.definitions._
    Seq(
      BooleanTpe
    )
  }

  override protected def supportedOperators: Set[String] = Set(
    "&&",
    "||"
  )

  override protected def mutationRules: Map[String, String] = Map(
    "&&" -> "||",
    "||" -> "&&"
  )
}
