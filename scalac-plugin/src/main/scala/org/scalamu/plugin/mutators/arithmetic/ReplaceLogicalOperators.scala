package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}
import org.scalamu.plugin.mutators.AbstractBinaryOperatorMutator

import scala.tools.nsc.Global

/**
 * Mutation operator that swaps logical operators `&&` and `||`.
 */
case object ReplaceLogicalOperators extends AbstractBinaryOperatorMutator {
  override def description: String = "Replaced logical operator && <=> ||"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new BinaryOperatorTransformer(config)(global) {
      override protected def operatorNameMapping: Map[String, String] = Map(
        "&&" -> "||",
        "||" -> "&&"
      )

      override protected def isApplicableType(tpe: global.Type): Boolean 
        = tpe =:= global.definitions.BooleanTpe
    }
}
