package org.scalamu.plugin.mutations.controllflow

/**
 * Mutation, that replaces boundaries on conditional operators { <, >, <=, >= }
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
case object ReplaceConditionalBoundaries extends ConditionalsMutation {
  override val description: String = "Modified conditional boundary"

  override protected def mutationRules = Map(
    "<"  -> "<=",
    ">"  -> ">=",
    "<=" -> "<",
    ">=" -> ">"
  )
}
