package org.scalamu.plugin.mutations.arithmetics

import org.scalamu.plugin.Mutation

import scala.tools.nsc.Global

/**
 * Base trait for all arithmetic related mutations.
 */
trait ArithmeticOperatorMutation extends Mutation {
  protected def supportedTypes(implicit global: Global): Seq[global.Type] = {
    import global.definitions._
    Seq(
      ByteTpe,
      ShortTpe,
      IntTpe,
      LongTpe,
      FloatTpe,
      DoubleTpe
    )
  }

  protected val supportedOperators = Set(
    "+",
    "-",
    "%",
    "/",
    "*",
    "|",
    "&",
    "^",
    "<<",
    ">>"
  )

  protected val mutationRules = Map(
    "+"  -> "-",
    "-"  -> "+",
    "/"  -> "*",
    "%"  -> "*",
    "*"  -> "/",
    "&"  -> "|",
    "|"  -> "&",
    "^"  -> "&",
    "<<" -> ">>",
    ">>" -> "<<"
  )

  protected def isAppropriatelyTyped(global: Global)(tree: global.Tree): Boolean = {
    import global._
    tree.tpe match {
      case TypeRef(tpe, sym, _) => supportedTypes(global).exists(_ =:= sym.asType.tpe)
      case ustpe: SingletonType => supportedTypes(global).exists(_ =:= ustpe.underlying)
    }
  }
}
