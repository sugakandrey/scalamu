package org.scalamu.plugin.mutators.literals
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

case object ReplaceLongLiterals extends AbstractLiteralMutator {
  override def description: String = """Replaced long literal "x" with x + 1."""

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new LiteralTransformer(config)(global) {
      import global._
      override protected def isApplicableType(tpe: Type): Boolean = tpe =:= definitions.LongTpe

      override protected def replaceWith(input: Literal): Tree = {
        val longValue   = input.value.longValue
        val replacement = longValue + 1
        Literal(Constant(replacement))
      }
    }
}
