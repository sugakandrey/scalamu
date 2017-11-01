package org.scalamu.report

import java.text.NumberFormat
import java.util.Locale

import org.scalamu.core.api.TestedMutant
import org.scalamu.core.coverage.Statement

import scala.collection.Set

trait CoverageStats {
  def statements: Set[Statement]
  def coveredStatements: Set[Statement]
  def mutants: Set[TestedMutant]

  private val formatter = {
    val fmt = NumberFormat.getInstance(Locale.ROOT)
    fmt.setMinimumFractionDigits(1)
    fmt.setMaximumFractionDigits(1)
    fmt
  }

  def coverage: String =
    if (statements.isEmpty) "100"
    else formatter.format(coveredStatements.size * 100.0 / statements.size)

  def mutationCoverage: String =
    if (mutants.isEmpty) "100"
    else formatter.format(mutants.count(_.status.killed) * 100.0 / mutants.size)
}
