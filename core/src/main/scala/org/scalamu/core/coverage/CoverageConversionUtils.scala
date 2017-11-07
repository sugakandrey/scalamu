package org.scalamu.core.coverage

import org.scalamu.common.MutationId
import org.scalamu.core.process.MeasuredSuite
import org.scalamu.plugin.MutationInfo
import org.scalamu.core.testapi.AbstractTestSuite

import scala.collection.breakOut
import scala.collection.{mutable => m}

object CoverageConversionUtils {
  def statementCoverageToInverseMutationCoverage(
    statementCoverage: Map[MeasuredSuite, Set[Statement]],
    mutations: Set[MutationInfo]
  ): Map[MutationId, Set[MeasuredSuite]] = {
    val statementsCoverageByFile = for {
      (info, statements) <- statementCoverage
    } yield info -> statements.groupBy(_.pos.source)

    mutations.map { mutant =>
      val source = mutant.pos.source
      val tests: Set[MeasuredSuite] = statementsCoverageByFile.collect {
        case (test, bySourceCov) if bySourceCov.get(source).exists(_.exists(_.pos.overlaps(mutant.pos))) =>
          test
      }(breakOut)
      mutant.id -> tests
    }(breakOut)
  }

  def inverseMutationCoverageToInverseFileCoverage(
    coverage: Map[MutationId, Set[MeasuredSuite]],
    mutationsById: Map[MutationId, MutationInfo]
  ): collection.Map[String, collection.Set[AbstractTestSuite]] = {
    val builder = new m.HashMap[String, m.Set[AbstractTestSuite]] with m.MultiMap[String, AbstractTestSuite]
    coverage.foreach {
      case (id, suites) =>
        val source = mutationsById(id).pos.source
        suites.foreach(s => builder.addBinding(source, s.suite))
    }
    builder
  }
}
