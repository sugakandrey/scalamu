package org.scalamu.plugin.mutators.literals
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

case object ReplaceIntegerLiterals extends AbstractLiteralMutator {
  override def description: String = """Replaced integer value "x" with if (x == 0) 1 else 0."""

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new LiteralTransformer(config)(global) {
      import global._

      override protected def isApplicableType(tpe: Type): Boolean =
        Seq(
          definitions.IntTpe,
          definitions.ByteTpe,
          definitions.ShortTpe
        ).exists(tpe =:= _)

      override protected def replaceWith(input: Literal): Tree = {
        val intValue    = input.value.intValue
        val replacement = if (intValue == 0) 1 else 0
        Literal(Constant(replacement))
      }
    }
}
