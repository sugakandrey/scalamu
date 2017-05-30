package org.scalamu.report

import org.scalamu.core._

final case class Line(
  contents: String,
  number: Int,
  mutants: Set[TestedMutant] = Set.empty
)
