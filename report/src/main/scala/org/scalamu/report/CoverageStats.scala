package org.scalamu.report

import org.scalamu.core.TestedMutant
import org.scalamu.core.coverage.Statement

trait CoverageStats {
  def statements: Set[Statement]
  def invokedStatements: Set[Statement]
  def mutants: Set[TestedMutant]

  def coverage: String =
    if (statements.isEmpty) "100"
    else (invokedStatements.size * 100.0 / statements.size).formatted("%.1f")

  def mutationCoverage: String =
    if (mutants.isEmpty) "100"
    else (mutants.count(_.status.killed) * 100.0 / mutants.size).formatted("%.1f")
}
