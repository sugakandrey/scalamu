package org.scalamu.plugin.mutators.literals
import scala.tools.nsc.Global

class ReplaceLongLiterals extends LiteralMutator {
  override def description: String = """Replaced long literal "x" with x + 1."""

  override protected def replaceWith(global: Global)(original: global.Constant): global.Constant = {
    import global._

    val longValue        = original.longValue
    val replacementValue = longValue + 1
    Constant(replacementValue)
  }

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(global.definitions.LongTpe)
}
