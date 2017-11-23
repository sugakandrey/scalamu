package org.scalamu.plugin.mutators

import org.scalamu.plugin.MutatingTransformer

/**
 * Base trait for mutation operators supporting primitive numeric types.
 */
trait NumericTypesSupport extends TypeAware { self: MutatingTransformer =>
  import global.definitions._

  protected val numericTypes = Seq(
    ByteTpe,
    ShortTpe,
    CharTpe,
    IntTpe,
    LongTpe,
    FloatTpe,
    DoubleTpe
  )

  override protected def isApplicableType(tpe: global.Type): Boolean = numericTypes.exists(tpe =:= _)
}
