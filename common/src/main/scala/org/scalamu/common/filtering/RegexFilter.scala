package org.scalamu.common.filtering

import scala.util.matching.Regex

object RegexFilter {
  def apply(acceptSymbols: Regex*): NameFilter =
    if (acceptSymbols.nonEmpty) new RegexFilter(acceptSymbols) else AcceptAllFilter
}

class RegexFilter(acceptSymbols: Seq[Regex]) extends NameFilter {
  override def accepts: (String) => Boolean =
    name => acceptSymbols.exists(_.pattern.matcher(name).matches())
}
