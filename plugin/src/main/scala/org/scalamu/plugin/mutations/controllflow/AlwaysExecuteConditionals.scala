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
  override val description: String = "Replaced conditional with \"then\" branch"

  override def mutatingTransformer(
    global: Global,
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutation: Mutation = self

    override protected def transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"if ($cond) $thenp else $elsep" =>
          val mutant      = thenp.duplicate
          val mutatedThen = super.transform(thenp)
          val mutatedElse = super.transform(elsep)
          val alternative = treeCopy.If(tree, cond, mutatedThen, mutatedElse)

          val id = generateMutantReport(tree, mutant)
          guard(mutant, alternative, id)
      }
    }
  }
}
