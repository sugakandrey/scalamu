package org.scalamu.plugin.mutators.literals

import org.scalamu.plugin.{MutatingTransformer, Mutator, ScalamuScalacConfig}
import org.scalamu.plugin.mutators.{AbstractReplacementRules, TypeAware}

import scala.tools.nsc.Global

trait AbstractLiteralMutator extends Mutator { self =>
  protected abstract class LiteralTransformer(config: ScalamuScalacConfig)(override val global: Global)
      extends MutatingTransformer(config)(global)
      with TypeAware
      with AbstractReplacementRules {
    import global._

    override type Domain = Literal
    
    override def mutator: Mutator = self

    override def isApplicableTo(input: Literal): Boolean = isApplicableType(input.value.tpe)

    override val transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case tree @ Literal(Constant(_)) if isApplicableTo(tree) =>
          val mutant = replaceWith(tree)
            .setType(tree.tpe.simplify)
            .setPos(tree.pos.makeTransparent)

          val id = generateMutantReport(tree, mutant)
          guard(mutant, tree, id)
      }
    }
  }
}
