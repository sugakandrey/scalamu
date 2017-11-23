package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.mutators.{AbstractBinaryOperatorMutator, NumericTypesSupport}
import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.runtime._
import scala.tools.nsc.Global

/**
 * Mutation operator that swaps `until` and `to` method calls on numeric types.
 * e.g.
 * {{{
 * (1 to 10).foreach(println)
 * }}}
 * is mutated to
 * {{{
 * (1 until 10).foreach(println)
 * }}}
 */
case object ChangeRangeBoundary extends AbstractBinaryOperatorMutator { self =>
  override val description: String = "Changed range boundary inclusive <=> exclusive"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new BinaryOperatorTransformer(config)(global) with NumericTypesSupport {
      import global._

      override protected def operatorNameMapping: Map[String, String] = Map(
        "until" -> "to",
        "to"    -> "until"
      )

      override protected def isApplicableType(tpe: Type): Boolean =
        super.isApplicableType(tpe) || Seq(
          typeOf[RichByte],
          typeOf[RichShort],
          typeOf[RichChar],
          typeOf[RichInt],
          typeOf[RichLong],
          typeOf[RichFloat],
          typeOf[RichDouble]
        ).exists(tpe =:= _)
    }
}
