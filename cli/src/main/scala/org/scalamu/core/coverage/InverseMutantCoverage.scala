package org.scalamu.core.coverage

import org.scalamu.common.MutantId
import org.scalamu.core.workers.MeasuredSuite
import org.scalamu.plugin.MutantInfo

import scala.collection.breakOut

object InverseMutantCoverage {
  def fromStatementCoverage(
    statementCoverage: Map[MeasuredSuite, Set[Statement]],
    mutants: Set[MutantInfo]
  ): Map[MutantId, Set[MeasuredSuite]] = {
    val statementsCoverageByFile = for {
      (info, statements) <- statementCoverage
    } yield info -> statements.groupBy(_.pos.source)

    mutants.map { mutant =>
      val source = mutant.pos.source
      val tests: Set[MeasuredSuite] = statementsCoverageByFile.collect {
        case (test, bySourceCov)
            if bySourceCov.get(source).exists(_.exists(_.pos.overlaps(mutant.pos))) =>
          test
      }(breakOut)
      mutant.id -> tests
    }(breakOut)
  }
}
