package org.scalamu.core.coverage

import org.scalamu.common.MutantId
import org.scalamu.core.process.MeasuredSuite
import org.scalamu.plugin.MutantInfo
import org.scalamu.core.testapi.AbstractTestSuite

import scala.collection.breakOut
import scala.collection.{mutable => m}

object CoverageConversionUtils {
  def statementCoverageToInverseMutantCoverage(
    statementCoverage: Map[MeasuredSuite, Set[Statement]],
    mutants: Set[MutantInfo]
  ): Map[MutantId, Set[MeasuredSuite]] = {
    val statementsCoverageByFile = for {
      (info, statements) <- statementCoverage
    } yield info -> statements.groupBy(_.pos.source)

    mutants.map { mutant =>
      val source = mutant.pos.source
      val tests: Set[MeasuredSuite] = statementsCoverageByFile.collect {
        case (test, bySourceCov) if bySourceCov.get(source).exists(_.exists(_.pos.overlaps(mutant.pos))) =>
          test
      }(breakOut)
      mutant.id -> tests
    }(breakOut)
  }

  def inverseMutantCoverageToInverseFileCoverage(
    coverage: Map[MutantId, Set[MeasuredSuite]],
    mutantsById: Map[MutantId, MutantInfo]
  ): collection.Map[String, collection.Set[AbstractTestSuite]] = {
    val builder = new m.HashMap[String, m.Set[AbstractTestSuite]] with m.MultiMap[String, AbstractTestSuite]
    coverage.foreach {
      case (id, suites) =>
        val source = mutantsById(id).pos.source
        suites.foreach(s => builder.addBinding(source, s.suite))
    }
    builder
  }
}
