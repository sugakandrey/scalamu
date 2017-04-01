package org.scalamu.plugin.mutations.controllflow

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
case object NegateConditionals extends ConditionalsMutation
