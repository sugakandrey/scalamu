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

    private def modifyType(tpe: Type, fromS: List[Symbol], toS: List[Symbol]): Type =
      if (tpe == null) null
      else {
        val kvs                 = replacements.toList
        val thisTypeSubstituted = kvs.foldLeft(tpe) { case (acc, (from, to)) => acc.substThis(from, to) }
        thisTypeSubstituted.substituteSymbols(fromS, toS)
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

      val kvs   = replacements.toList
      val fromS = kvs.map(_._1)
      val toS   = kvs.map(_._2)

      t1.setType(modifyType(t1.tpe, fromS, toS))

      replacements
        .get(t1.symbol)
        .foreach(
          replacement =>
            try { t1.symbol = replacement } catch {
              case _: UnsupportedOperationException =>
                scribe.debug(s"Unable to apply symbol_= to class = ${t1.getClass} in ${t1.pos}.")
          }
        )

      Option(t1.symbol).foreach { s =>
        val oldScope = s.info.decls
        val newInfo = (s.info match {
          case ClassInfoType(parents, scope, ts) =>
            internal.classInfoType(parents, scope.cloneScope, replacements.getOrElse(ts, ts))
          case RefinedType(parents, scope) => internal.refinedType(parents, scope.cloneScope)
          case info                        => info
        }).substituteSymbols(fromS, toS)

        val validTo = s.validTo
        s.info    = newInfo
        s.validTo = validTo
        val scope = newInfo.decls
        replacements.foreach {
          case (from, to) =>
            val inScope = scope.lookup(from.name)
            if (inScope.id == from.id && (scope ne oldScope)) {
              scope.unlink(from)
              scope.enterIfNew(to)
            }
        }
      }

      t1.setType(modifyType(t1.tpe, fromS, toS))
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
