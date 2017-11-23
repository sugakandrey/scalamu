package org.scalamu.plugin.mutators.controllflow
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

/**
 * Mutation operator that replaces boundaries on conditional operators { <, >, <=, >= }
 * with their counterparts.
 * e.g.
 * {{{
 * if (foo > bar) {
 *   ...
 * }
 * }}}
 * is mutated to
 * {{{
 * if (foo <= bar) {
 *   ...
 * }
 * }}}
 *
 */
case object ChangeConditionalBoundaries extends AbstractRelationalOperatorMutator {
  override val description: String = "Modified conditional boundary."

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new RelationalOperatorTransformer(config)(global) {
      override protected def operatorNameMapping: Map[String, String] = Map(
        "<"  -> "<=",
        ">"  -> ">=",
        "<=" -> "<",
        ">=" -> ">"
      )
    }
}
