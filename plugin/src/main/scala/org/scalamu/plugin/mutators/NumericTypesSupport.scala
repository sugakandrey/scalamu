package org.scalamu.plugin.mutators

import org.scalamu.plugin.Mutator

import scala.tools.nsc.Global

/**
 * Base trait for mutation operators supporting primitive numeric types.
 */
trait NumericTypesSupport extends SupportedTypes { self: Mutator =>
  override protected def supportedTypes(implicit global: Global): Seq[global.Type] = {
    import global.definitions._
    Seq(
      ByteTpe,
      ShortTpe,
      CharTpe,
      IntTpe,
      LongTpe,
      FloatTpe,
      DoubleTpe
    )
  }
}
