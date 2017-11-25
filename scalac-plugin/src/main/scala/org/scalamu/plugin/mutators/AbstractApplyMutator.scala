package org.scalamu.plugin.mutators

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Base trait for mutation operators, which are applicable to `apply` method call trees.
 */
trait AbstractApplyMutator extends Mutator { self =>
  protected abstract class ApplyTransformer(config: ScalamuScalacConfig)(override val global: Global)
      extends MutatingTransformer(config)(global)
      with AbstractReplacementRules
      with TypeAware {
    import global._

    override type Domain = Tree

    override protected def mutator: Mutator = self

    override def isApplicableTo(input: Tree): Boolean = input match {
      case TreeWithType(_, tpe) => isApplicableType(tpe)
      case _                    => false
    }

    override def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"$qual.apply[$targs](...$args)" if isApplicableTo(qual) && args.nonEmpty =>
          q"${replaceWith(tree)}[$targs]".setPos(tree.pos.makeTransparent)
      }
    }
  }
}
