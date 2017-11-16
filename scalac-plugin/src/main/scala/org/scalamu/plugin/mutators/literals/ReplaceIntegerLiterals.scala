package org.scalamu.plugin.mutators.literals
import scala.tools.nsc.Global

case object ReplaceIntegerLiterals extends LiteralMutator {
  override def description: String = """Replaced integer value "x" with if (x == 0) 1 else 0."""

  override protected def replaceWith(global: Global)(original: global.Constant): global.Constant = {
    import global._

    val intValue         = original.intValue
    val replacementValue = if (intValue == 0) 1 else 0
    Constant(replacementValue)
  }

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(
      global.definitions.IntTpe,
      global.definitions.ByteTpe,
      global.definitions.ShortTpe
    )
}
