package org.scalamu.plugin.mutators

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Base trait for mutation operators, which are only applicable to syntactic trees matching
 * the following patter `Apply(Select(lhs, op @ TermName(_)) List(arg) `, where `op`
 * is replaced according to [[org.scalamu.plugin.mutators.OperatorMutationRules.mutationRules]]
 */
trait BinaryOperatorMutator extends Mutator with OperatorMutationRules with SupportedTypes {
  self =>

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutator: Mutator = self

    private def isAppropriateType(tpe: Type): Boolean = supportedTypes.exists(_ =:= tpe)

    override protected def transformer: Transformer = new Transformer {
      override val mutate: PartialFunction[Tree, Tree] = {
        case tree @ Apply(
              Select(TreeWithType(lhs, lhsTpe), op @ TermName(_)),
              List(TreeWithType(rhs, rhsTpe))
            )
            if mutationRules.contains(op.decodedName.toString)
              && isAppropriateType(lhsTpe)
              && isAppropriateType(rhsTpe) =>
          val mutatedOp = encode(mutationRules(op.decodedName.toString))
          val mutant =
            q"${lhs.safeDuplicate}.$mutatedOp(${rhs.safeDuplicate})".setPos(tree.pos.makeTransparent)

          val mutatedLhs = super.transform(lhs)
          val mutatedRhs = super.transform(rhs)

          val id = generateMutantReport(tree, mutant)

          guard(
            mutant,
            q"$mutatedLhs.$op(..${List(mutatedRhs)})".setPos(tree.pos.makeTransparent),
            id
          )
      }
    }
  }
}
