package org.scalamu.core.compilation


import org.scalamu.common.filtering.RegexBasedFilter

import scala.util.matching.Regex

final case class IgnoreCoverageStatementsFilter(filters: Seq[Regex])
    extends RegexBasedFilter(IgnoreCoverageStatementsFilter.coverageStatementsFilter +: filters)

object IgnoreCoverageStatementsFilter {
  private val coverageStatementsFilter = """scoverage.*""".r
}
