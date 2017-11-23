package org.scalamu.plugin.mutators.literals
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

case object ReplaceFloatingPointLiterals extends AbstractLiteralMutator {
  override def description: String =
    """Replaced floating point literal "x" with if (Double.isFinite(x)) -(x + 1.0) else 0."""

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new LiteralTransformer(config)(global) {
      import global._

      override protected def replaceWith(input: Literal): Tree = {
        import global._

        val doubleValue      = input.value.doubleValue
        val replacementValue = if (java.lang.Double.isFinite(doubleValue)) -(doubleValue + 1.0) else 0.0
        Literal(Constant(replacementValue))
      }

      override protected def isApplicableType(tpe: Type): Boolean =
        Seq(
          global.definitions.FloatTpe,
          global.definitions.DoubleTpe
        ).exists(tpe =:= _)
    }
}
