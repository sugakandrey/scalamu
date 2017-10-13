package org.scalamu.plugin.util

import scala.collection.Map

trait TreeEnrichment { self: CompilerAccess =>
  import global._

  implicit class RichTree(tree: Tree) {
    def collectSymbolReplacements: Map[Symbol, Symbol] = {
      import scala.collection.{mutable => m}
      val replacements = m.HashMap.empty[Symbol, Symbol]

      tree.collect {
        case t @ (_: DefTree | _: Function) =>
          val replacement = replacements.getOrElse(t.symbol, t.symbol.cloneSymbol)
          replacements.get(replacement.owner).foreach(replacement.owner = _)
          replacements += t.symbol -> replacement
      }

      replacements
    }

    def copySymbols(replacements: Map[Symbol, Symbol]): Unit =
      tree.collect {
        case t if t.hasExistingSymbol =>
          replacements.get(t.symbol).foreach(t.symbol = _)
      }
    
    def copySymbols(other: Tree): Unit = copySymbols(other.collectSymbolReplacements)
  }
}
