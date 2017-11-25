package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation operator that guarantees that conditional blocks always execute.
 * e.g.
 * {{{
 * if (cond()) {
 *   foo()
 * } else {
 *   bar()
 * }
 * }}}
 * is mutated to
 * {{{
 * foo()
 * }}}
 */
case object AlwaysExecuteConditionals extends AbstractConditionalMutator {
  override val description: String = "Replaced conditional with its \"then\" branch"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new ConditionalTransformer(config)(global) {
      import global._

      override protected def replaceWith(input: If): Tree = input.thenp.safeDuplicate
    }
}
