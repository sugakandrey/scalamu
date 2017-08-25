package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.mutators.BinaryOperatorMutator

import scala.tools.nsc.Global

/**
 * Mutation operator that swaps logical operators `&&` and `||`.
 */
case object ReplaceLogicalOperators extends BinaryOperatorMutator {
  override def description: String = "Replaced logical operator && <=> ||"

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(global.definitions.BooleanTpe)

  override protected val mutationRules: Map[String, String] = Map(
    "&&" -> "||",
    "||" -> "&&"
  )
}
