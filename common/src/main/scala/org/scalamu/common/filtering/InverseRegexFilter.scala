package org.scalamu.common.filtering

import scala.util.matching.Regex

object InverseRegexFilter {
  def apply(ignoreSymbols: Regex*): InverseRegexFilter = new InverseRegexFilter(ignoreSymbols)
}

class InverseRegexFilter(ignoreSymbols: Seq[Regex]) extends NameFilter {
  override protected def accepts: (String) => Boolean =
    name => ignoreSymbols.forall(!_.pattern.matcher(name).matches())
}
