package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.mutators.AbstractReplacementRules
import org.scalamu.plugin.{MutatingTransformer, Mutator, ScalamuScalacConfig}

import scala.tools.nsc.Global

trait AbstractConditionalMutator extends Mutator { self =>
  protected abstract class ConditionalTransformer(config: ScalamuScalacConfig)(override val global: Global)
      extends MutatingTransformer(config)(global)
      with AbstractReplacementRules {
    import global._

    override protected type Domain = Tree

    override protected def mutator: Mutator = self

    override val transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"if ($cond) $thenp else $elsep" =>
          val pos             = cond.pos.makeTransparent
          val mutant          = replaceWith(cond).setPos(pos)
          val id              = generateMutantReport(cond, mutant)
          val guardedMutation = typer.typed(guard(mutant, cond, id))

          val mutatedThen = super.transform(thenp)
          val mutatedElse = super.transform(elsep)

          treeCopy.If(tree, guardedMutation, mutatedThen, mutatedElse)
      }
    }
  }
}
