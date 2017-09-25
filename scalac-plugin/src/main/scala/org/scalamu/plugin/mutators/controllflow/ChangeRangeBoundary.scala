package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.mutators.{BinaryOperatorMutator, NumericTypesSupport}

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
case object ChangeRangeBoundary extends BinaryOperatorMutator with NumericTypesSupport { self =>
  override val description: String = "Changed range boundary inclusive <=> exclusive"

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] = {
    import global._
    super.supportedTypes ++ Seq(
      typeOf[RichByte],
      typeOf[RichShort],
      typeOf[RichChar],
      typeOf[RichInt],
      typeOf[RichLong],
      typeOf[RichFloat],
      typeOf[RichDouble]
    )
  }

  override protected def mutationRules: Map[String, String] = Map(
    "until" -> "to",
    "to"    -> "until"
  )
}