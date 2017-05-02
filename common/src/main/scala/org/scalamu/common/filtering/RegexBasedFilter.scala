package org.scalamu.common.filtering

import scala.util.matching.Regex

object RegexBasedFilter {
  def apply(ignoreSymbols: Regex*): RegexBasedFilter = new RegexBasedFilter(ignoreSymbols)
}

class RegexBasedFilter(ignoreSymbols: Seq[Regex]) extends NameFilter {
  override def isSymbolIgnored(symbolName: String): Boolean =
    ignoreSymbols.exists(_.pattern.matcher(symbolName).matches())
}
