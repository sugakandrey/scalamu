package org.scalamu.plugin.mutators.literals
import scala.tools.nsc.Global

object ReplaceFloatingPointLiterals extends LiteralMutator {
  override def description: String =
    """Replaced floating point literal "x" with if (Double.isFinite(x)) -(x + 1.0) else 0."""

  override protected def replaceWith(global: Global)(original: global.Constant): global.Constant = {
    import global._

    val doubleValue      = original.doubleValue
    val replacementValue = if (java.lang.Double.isFinite(doubleValue)) -(doubleValue + 1.0) else 0.0
    Constant(replacementValue)
  }

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(
      global.definitions.FloatTpe,
      global.definitions.DoubleTpe
    )
}
