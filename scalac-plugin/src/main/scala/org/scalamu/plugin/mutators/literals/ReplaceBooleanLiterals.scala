package org.scalamu.plugin.mutators.literals
import scala.tools.nsc.Global

case object ReplaceBooleanLiterals extends LiteralMutator {
  override def description: String = "Replaced boolean value with its negation."

  override protected def replaceWith(global: Global)(original: global.Constant): global.Constant = {
    import global._
    
    val booleanValue  = original.booleanValue
    Constant(!booleanValue)
  }

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(global.definitions.BooleanTpe)
}
