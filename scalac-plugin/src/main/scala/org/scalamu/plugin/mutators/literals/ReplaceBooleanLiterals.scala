package org.scalamu.plugin.mutators.literals
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

case object ReplaceBooleanLiterals extends AbstractLiteralMutator {
  override def description: String = "Replaced boolean value with its negation."

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new LiteralTransformer(config)(global) {
      import global._

      override protected def isApplicableType(tpe: Type): Boolean = tpe =:= definitions.BooleanTpe
      override protected def replaceWith(input: Literal): Tree   = Literal(Constant(!input.value.booleanValue))
    }
}
