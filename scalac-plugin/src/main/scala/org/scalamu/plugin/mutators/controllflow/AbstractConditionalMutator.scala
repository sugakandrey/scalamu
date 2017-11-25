package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.mutators.AbstractReplacementRules
import org.scalamu.plugin.{MutatingTransformer, Mutator, ScalamuScalacConfig}

import scala.tools.nsc.Global

trait AbstractConditionalMutator extends Mutator { self =>
  protected abstract class ConditionalTransformer(config: ScalamuScalacConfig)(override val global: Global)
      extends MutatingTransformer(config)(global)
      with AbstractReplacementRules {
    import global._

    override protected type Domain = If

    override protected def mutator: Mutator = self

    override val transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case tree: If => replaceWith(tree).setPos(tree.pos.makeTransparent)
      }
    }
  }
}
