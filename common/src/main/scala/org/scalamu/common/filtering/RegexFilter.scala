package org.scalamu.common.filtering

import scala.util.matching.Regex

object RegexFilter {
  def apply(acceptSymbols: Regex*): RegexFilter = new RegexFilter(acceptSymbols)
}

class RegexFilter(acceptSymbols: Seq[Regex]) extends NameFilter {
  override protected def accepts: (String) => Boolean =
    name => acceptSymbols.exists(_.pattern.matcher(name).matches())
}
