package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.mutators.NumericTypesSupport
import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation operator thar inverts negations of integer and floating point numbers.
 * e.g.
 * {{{
 * val a = -b
 * }}}
 * is mutated to
 * {{{
 * val a = b
 * }}}
 */
case object InvertNegations extends Mutator { self =>
  override val description: String = "Removed numeric negation"

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) with NumericTypesSupport {
    import global._

    override protected def mutator: Mutator = self

    override protected def transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"${lit: Constant}" if lit.isNumeric && lit.doubleValue < 0 =>
          val value: Any = lit.value match {
            case v: Byte =>
              if (v == Byte.MinValue) Byte.MaxValue
              else -lit.byteValue
            case v: Short =>
              if (v == Short.MinValue) Short.MaxValue
              else -lit.shortValue
            case v: Int =>
              if (v == Int.MinValue) Int.MaxValue
              else -lit.intValue
            case v: Long =>
              if (v == Long.MinValue) Long.MaxValue
              else -lit.longValue
            case _: Float  => -lit.doubleValue
            case _: Double => -lit.floatValue
          }
          val mutant = Literal(Constant(value)).setPos(tree.pos)
          mutant.setType(tree.tpe.simplify)
          val id = generateMutantReport(tree, mutant)
          guard(mutant, tree, id)
        case tree @ q"-${TreeWithType(term, tpe)}" if isApplicableType(tpe) =>
          val mutant      = term.safeDuplicate
          val alternative = super.transform(term)
          val id          = generateMutantReport(tree, mutant)
          guard(mutant, q"-$alternative", id)
      }
    }
  }
}
