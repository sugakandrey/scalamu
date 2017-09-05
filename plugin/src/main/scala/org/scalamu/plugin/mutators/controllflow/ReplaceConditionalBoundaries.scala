package org.scalamu.plugin.mutators.controllflow

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
case object ReplaceConditionalBoundaries extends ConditionalsMutator {
  override val description: String = "Modified conditional boundary"

  override protected def mutationRules = Map(
    "<"  -> "<=",
    ">"  -> ">=",
    "<=" -> "<",
    ">=" -> ">"
  )
}
