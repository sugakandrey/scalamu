package org.scalamu.plugin

import scala.util.matching.Regex

trait MutationFilter extends (String => Boolean) {
  override def apply(symbolName: String): Boolean = isSymbolIgnored(symbolName)
  def isSymbolIgnored(symbolName: String): Boolean
}

case object AcceptAllFilter extends MutationFilter {
  override def isSymbolIgnored(symbolName: String): Boolean = false
}

class RegexBasedFilter(ignoreSymbols: Seq[Regex]) extends MutationFilter {
  override def isSymbolIgnored(symbolName: String): Boolean =
    ignoreSymbols.exists(_.pattern.matcher(symbolName).matches())
}

object RegexBasedFilter {
  def apply(ignoreSymbols: Regex*): RegexBasedFilter = new RegexBasedFilter(ignoreSymbols)
}
