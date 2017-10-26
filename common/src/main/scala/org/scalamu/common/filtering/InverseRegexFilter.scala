package org.scalamu.common.filtering

import scala.util.matching.Regex

object InverseRegexFilter {
  def apply(ignore: Regex*): NameFilter =
    if (ignore.nonEmpty) new InverseRegexFilter(ignore) else AcceptAllFilter
}

class InverseRegexFilter(ignore: Seq[Regex]) extends NameFilter {
  override def accepts: (String) => Boolean =
    name => ignore.forall(!_.pattern.matcher(name).matches())
}
