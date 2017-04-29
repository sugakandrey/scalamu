package org.scalamu.core.coverage

import org.scalamu.plugin.MutantInfo
import org.scalamu.testapi.AbstractTestSuite

import scala.collection.breakOut

object InverseMutantCoverage {
  def fromStatementCoverage(
    statementCoverage: Map[AbstractTestSuite, Set[Statement]],
    mutants: Set[MutantInfo]
  ): Map[MutantInfo, Set[AbstractTestSuite]] = {
    val statementsCoverageByFile = for {
      (info, statements) <- statementCoverage
    } yield info -> statements.groupBy(_.source)

    mutants.map { mutant =>
      val source = mutant.pos.source.path
      val tests: Set[AbstractTestSuite] = statementsCoverageByFile.collect {
        case (test, bySourceCov)
            if bySourceCov.get(source).exists(_.exists(_.pos.includes(mutant))) =>
          test
      }(breakOut)
      mutant -> tests
    }(breakOut)
  }
}
