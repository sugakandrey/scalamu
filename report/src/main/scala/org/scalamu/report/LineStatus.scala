package org.scalamu.report

sealed abstract class LineStatus(val styleName: String)

object LineStatus {
  case object Covered       extends LineStatus("covered")
  case object NotCovered    extends LineStatus("uncovered")
  case object NotApplicable extends LineStatus("na")
}
