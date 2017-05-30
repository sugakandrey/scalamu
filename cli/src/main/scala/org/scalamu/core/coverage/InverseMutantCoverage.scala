package org.scalamu.core.coverage

import org.scalamu.common.MutantId
import org.scalamu.plugin.MutantInfo
import org.scalamu.testapi.AbstractTestSuite

import scala.collection.breakOut

object InverseMutantCoverage {
  def fromStatementCoverage(
    statementCoverage: Map[AbstractTestSuite, Set[Statement]],
    mutants: Set[MutantInfo]
  ): Map[MutantId, Set[AbstractTestSuite]] = {
    val statementsCoverageByFile = for {
      (info, statements) <- statementCoverage
    } yield info -> statements.groupBy(_.pos.source)

    mutants.map { mutant =>
      val source = mutant.pos.source
      val tests: Set[AbstractTestSuite] = statementsCoverageByFile.collect {
        case (test, bySourceCov)
            if bySourceCov.get(source).exists(_.exists(_.pos.overlaps(mutant.pos))) =>
          test
      }(breakOut)
      mutant.id -> tests
    }(breakOut)
  }
}
