package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.mutations.BinaryOperatorMutation

import scala.tools.nsc.Global

/**
 * Mutation, that swaps logical operators `&&` and `||`.
 */
case object ReplaceLogicalOperators extends BinaryOperatorMutation {
  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(global.definitions.BooleanTpe)

  override protected val mutationRules: Map[String, String] = Map(
    "&&" -> "||",
    "||" -> "&&"
  )
}
