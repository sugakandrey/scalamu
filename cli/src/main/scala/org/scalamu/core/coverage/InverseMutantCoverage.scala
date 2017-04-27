package org.scalamu.core.coverage

import org.scalamu.plugin.MutantInfo
import org.scalamu.testapi.AbstractTestSuite

object InverseMutantCoverage {
  def fromStatementCoverage(
    statementCoverage: Map[AbstractTestSuite, Set[Statement]],
    mutants: Set[MutantInfo]
  ): Map[MutantInfo, Set[AbstractTestSuite]] = {
    val statementsCoverageByFile = for {
      (info, statements) <- statementCoverage
    } yield info -> statements.groupBy(_.source)

    (for {
      mutant <- mutants
      source = mutant.pos.source.path
      tests = statementsCoverageByFile.collect {
        case (test, bySourceCov)
            if bySourceCov.get(source).exists(_.exists(_.pos.includes(mutant))) =>
          test
      }.toSet
    } yield mutant -> tests)(collection.breakOut)
  }
}
