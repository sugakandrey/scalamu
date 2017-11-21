package org.scalamu.plugin.util

import scala.collection.{mutable => m}

trait TreeEnrichment { self: CompilerAccess =>
  import global._

  lazy val safeDuplicator = new SafeDuplicator

  private[plugin] class SafeDuplicator extends Transformer {
    override val treeCopy: TreeCopier = newStrictTreeCopier
    private val replacements          = m.HashMap.empty[Symbol, Symbol]
    private val introducedSymbols     = m.HashSet.empty[Symbol]

    def reset(): Unit = {
      replacements.clear()
      introducedSymbols.clear()
    }

    override def transform(t: Tree): Tree = {
      t match {
        case t @ (_: DefTree | _: Function) if t.hasExistingSymbol => introducedSymbols += t.symbol
        case _                                                     => ()
      }

      if (t.hasExistingSymbol && introducedSymbols(t.symbol)) {
        val oldSymbol = t.symbol
        val cloned    = replacements.getOrElse(oldSymbol, oldSymbol.cloneSymbol)
        replacements.get(oldSymbol.owner).foreach(cloned.owner = _)
        replacements += oldSymbol -> cloned
      }

      val t1 = super.transform(t)

      if ((t1 ne t) && t1.pos.isRange) t1.setPos(t.pos.focus)

      replacements.get(t1.symbol).foreach(replacement =>
        try { t1.symbol = replacement } catch { 
          case _: UnsupportedOperationException => 
            scribe.debug(s"Unable to apply symbol_= to class = ${t1.getClass} in ${t1.pos}.")
        }
      )

      t1
    }
  }

  implicit class RichTree(tree: Tree) {
    def safeDuplicate: Tree = {
      val res = safeDuplicator.transform(tree)
      safeDuplicator.reset()
      res
    }
  }
}
