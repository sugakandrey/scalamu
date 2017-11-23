package org.scalamu.plugin.mutators.controllflow
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

/**
 * Mutation, that replaces conditional operators with their logical counterparts.
 * e.g.
 * {{{
 * if (a > 10) {
 *   ..
 * } else if (c == 10) {
 *   ..
 * }
 * }}}
 * is replaced with
 * {{{
 * if (a <= 10) {
 *   ..
 * } else if (c != 10) {
 *   ..
 * }
 * }}}
 */
case object NegateConditionals extends AbstractRelationalOperatorMutator {
  override val description: String = "Negated conditional operator"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new RelationalOperatorTransformer(config)(global) {
      override protected def operatorNameMapping: Map[String, String] = Map(
        ">"  -> "<=",
        "<"  -> ">=",
        ">=" -> "<",
        "<=" -> ">",
        "==" -> "!=",
        "!=" -> "=="
      )
    }
}
