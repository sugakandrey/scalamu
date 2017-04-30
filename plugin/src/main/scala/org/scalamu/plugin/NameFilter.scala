package org.scalamu.plugin

import scala.util.matching.Regex

trait NameFilter extends (String => Boolean) {
  override def apply(symbolName: String): Boolean = isSymbolIgnored(symbolName)
  def isSymbolIgnored(symbolName: String): Boolean
}

case object AcceptAllFilter extends NameFilter {
  override def isSymbolIgnored(symbolName: String): Boolean = false
}

class RegexBasedFilter(ignoreSymbols: Seq[Regex]) extends NameFilter {
  override def isSymbolIgnored(symbolName: String): Boolean =
    ignoreSymbols.exists(_.pattern.matcher(symbolName).matches())
}

object RegexBasedFilter {
  def apply(ignoreSymbols: Regex*): RegexBasedFilter = new RegexBasedFilter(ignoreSymbols)
}
