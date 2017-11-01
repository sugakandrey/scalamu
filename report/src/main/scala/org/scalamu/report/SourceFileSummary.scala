package org.scalamu.report

import org.scalamu.core.coverage.Statement
import org.scalamu.core.api.{SourceInfo, TestedMutant}
import org.scalamu.core.testapi.AbstractTestSuite

import scala.io.Source
import scala.collection.{Map, Set, SortedSet}

final case class SourceFileSummary(
  name: String,
  lines: Seq[Line],
  statements: Set[Statement],
  coveredStatements: Set[Statement],
  mutantsByLine: Map[Int, Set[TestedMutant]],
  exercisedTests: SortedSet[AbstractTestSuite]
) extends CoverageStats {
  override def mutants: Set[TestedMutant] = mutantsByLine.values.flatMap(identity)(collection.breakOut)
}

object SourceFileSummary {
  def apply(
    info: SourceInfo,
    statements: Set[Statement],
    invoked: Set[Statement],
    mutants: Set[TestedMutant],
    tests: Set[AbstractTestSuite]
  ): SourceFileSummary = {
    val mutantsByLine = mutants.groupBy(_.info.pos.line)
    val lines = Source
      .fromFile(info.fullPath.toFile, "utf-8")
      .getLines()
      .zipWithIndex
      .map {
        case (contents, number) =>
          val lineNumber = number + 1
          Line(contents, lineNumber, mutantsByLine.getOrElse(lineNumber, Set.empty))
      }
      .toSeq

    implicit val testSuiteOrdering: Ordering[AbstractTestSuite] =
      Ordering.by[AbstractTestSuite, String](_.toString)

    SourceFileSummary(
      info.name.toString,
      lines,
      statements,
      invoked,
      mutantsByLine,
      tests.to[SortedSet]
    )
  }
}
