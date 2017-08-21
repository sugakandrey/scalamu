package org.scalamu.common.filtering

import scala.util.matching.Regex

object InverseRegexFilter {
  def apply(ignoreSymbols: Regex*): NameFilter =
    if (ignoreSymbols.nonEmpty) new InverseRegexFilter(ignoreSymbols) else AcceptAllFilter
}

class InverseRegexFilter(ignoreSymbols: Seq[Regex]) extends NameFilter {
  override def accepts: (String) => Boolean =
    name => ignoreSymbols.forall(!_.pattern.matcher(name).matches())
}
