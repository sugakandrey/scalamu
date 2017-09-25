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
case object AlwaysExecuteConditionals extends ConditionalsMutator { self =>
  override val description: String = "Replaced conditional with its \"then\" branch"

  override def mutatingTransformer(
    global: Global,
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutator: Mutator = self

    override protected def transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"if ($cond) $thenp else $elsep" =>
          val mutation        = q"true"
          val id              = generateMutantReport(cond, mutation)
          val guardedMutation = typer.typed(guard(mutation, cond, id)).setPos(cond.pos.makeTransparent)

          val mutatedThen = super.transform(thenp)
          val mutatedElse = super.transform(elsep)

          treeCopy.If(tree, guardedMutation, mutatedThen, mutatedElse)
      }
    }
  }
}