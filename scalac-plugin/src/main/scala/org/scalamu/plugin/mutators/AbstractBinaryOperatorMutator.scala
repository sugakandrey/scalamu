package org.scalamu.plugin.mutators

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Base trait for mutation operators, which are only applicable to syntactic trees matching
 * the following patter `Apply(Select(lhs, op @ TermName(_)) List(arg) `, where `op`
 * is replaced according to [[org.scalamu.plugin.mutators.AbstractReplacementRules.mutationRules]]
 */
trait AbstractBinaryOperatorMutator extends Mutator { self =>

  protected abstract class BinaryOperatorTransformer(config: ScalamuScalacConfig)(override val global: Global)
      extends MutatingTransformer(config)(global)
      with DomainAware
      with TypeAware {
    import global._
    protected def operatorNameMapping: Map[String, String]

    override type Domain = Apply

    override protected def mutator: Mutator = self

    override def isApplicableTo(input: Apply): Boolean = input match {
      case Apply(
          Select(TreeWithType(lhs, lhsTpe), op @ TermName(_)),
          List(TreeWithType(rhs, rhsTpe))
          ) =>
        operatorNameMapping.contains(op.decodedName.toString) && isApplicableType(lhsTpe) && isApplicableType(rhsTpe)
      case _ => false
    }

    override def transformer: Transformer = new Transformer {
      override val mutate: PartialFunction[Tree, Tree] = {
        case tree @ Apply(
              Select(TreeWithType(lhs, lhsTpe), op @ TermName(_)),
              List(TreeWithType(rhs, rhsTpe))
            ) if isApplicableTo(tree) =>
          val mutatedOp = encode(operatorNameMapping(op.decodedName.toString))
          val pos       = tree.pos.makeTransparent
          val mutant    = q"${lhs.safeDuplicate}.$mutatedOp(${rhs.safeDuplicate})".setPos(pos)

          val mutatedLhs = super.transform(lhs)
          val mutatedRhs = super.transform(rhs)

          val id          = generateMutantReport(tree, mutant)
          val alternative = q"$mutatedLhs.$op(..${List(mutatedRhs)})".setPos(pos)

          guard(mutant, alternative, id)
      }
    }
  }
}
