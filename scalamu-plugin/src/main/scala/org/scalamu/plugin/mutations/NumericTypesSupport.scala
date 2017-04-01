package org.scalamu.plugin.mutations

import org.scalamu.plugin.Mutation

import scala.tools.nsc.Global

/**
 * Base trait for mutations supporting primitive numeric types.
 */
trait NumericTypesSupport extends SupportedTypes { self: Mutation =>
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
