package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation, that guarantees that conditional blocks always execute.
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
case object AlwaysExecuteConditionals extends ConditionalsMutation { self =>
  override def mutatingTransformer(
    global: Global,
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutation: Mutation = self

    override protected def transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case q"if ($cond) $thenp else $elsep" =>
          val mutationResult = q"true"
          val guarded        = guard(mutationResult, cond)
          val mutatedThen    = super.transform(thenp)
          val mutatedElse    = super.transform(elsep)
          reportMutation(cond, mutationResult)
          q"if ($guarded) $mutatedThen else $mutatedElse"
      }
    }
  }
}
