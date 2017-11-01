package org.scalamu.report

import org.scalamu.core._
import org.scalamu.core.api.TestedMutant

import scala.collection.Set

final case class Line(
  contents: String,
  number: Int,
  mutants: Set[TestedMutant] = Set.empty
)
