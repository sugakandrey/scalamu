package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation operator that guarantees that conditional blocks never execute.
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
 * bar()
 * }}}
 */
case object NeverExecuteConditionals extends AbstractConditionalMutator {
  override val description: String = "Replaced conditional with its \"else\" branch"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new ConditionalTransformer(config)(global) {
      import global._
      
      override protected def replaceWith(input: Tree): Tree = q"false"
    }
}
