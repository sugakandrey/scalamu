package org.scalamu.report

import java.io.File

import org.scalamu.core.{SourceInfo, TestedMutant}
import org.scalamu.core.coverage.Statement
import org.scalamu.testapi.AbstractTestSuite

import scala.collection.{Map, Set}

final case class PackageSummary(
  name: String,
  override val statements: Set[Statement],
  override val coveredStatements: Set[Statement],
  override val mutants: Set[TestedMutant],
  sourceFiles: Set[SourceFileSummary]
) extends CoverageStats {
  def path: String = name.replaceAll("\\.", "\\" + File.separator)
}

object PackageSummary {
  def apply(
    name: String,
    statements: Set[Statement],
    invokedStatements: Set[Statement],
    mutants: Set[TestedMutant],
    inverseFileCoverage: Map[String, Set[AbstractTestSuite]],
    sources: Set[SourceInfo]
  ): PackageSummary =
    PackageSummary(
      name,
      statements,
      invokedStatements,
      mutants,
      sources.map(
        s =>
          SourceFileSummary(
            s,
            statements.filter(_.location.sourcePath == s.fullPath.toString),
            invokedStatements.filter(_.location.sourcePath == s.fullPath.toString),
            mutants.filter(_.info.pos.source == s.fullPath.toString),
            inverseFileCoverage.getOrElse(s.fullPath.toString, Set.empty)
        )
      )
    )
}
