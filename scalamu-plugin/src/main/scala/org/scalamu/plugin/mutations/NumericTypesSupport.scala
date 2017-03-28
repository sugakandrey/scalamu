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

  override protected def isAppropriatelyTyped(global: Global)(tree: global.Tree): Boolean = {
    import global._
    tree.tpe match {
      case TypeRef(tpe, sym, _) => supportedTypes(global).exists(_ =:= sym.asType.tpe)
      case ustpe: SingletonType => supportedTypes(global).exists(_ =:= ustpe.underlying)
    }
  }
}
