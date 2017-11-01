package org.scalamu.report

import org.scalamu.core.api.{SourceInfo, TestedMutant}
import org.scalamu.core.coverage.Statement
import org.scalamu.core.testapi.AbstractTestSuite

import scala.collection.{Map, Set}

final case class ProjectSummary private (
  override val statements: Set[Statement],
  override val coveredStatements: Set[Statement],
  override val mutants: Set[TestedMutant],
  packages: Set[PackageSummary]
) extends CoverageStats

object ProjectSummaryFactory {
  def apply(
    statements: Set[Statement],
    invokedStatements: Set[Statement],
    mutants: Set[TestedMutant],
    sourceFiles: Set[SourceInfo],
    inverseFileCoverage: Map[String, Set[AbstractTestSuite]]
  ): ProjectSummary = {
    val packages: Set[PackageSummary] =
      statements
        .groupBy(_.location.packageName)
        .map {
          case (name, stms) =>
            val invoked        = invokedStatements.filter(_.location.packageName == name)
            val packageSources = stms.map(_.location.sourcePath)
            val packageTests   = inverseFileCoverage.filterKeys(packageSources)
            val sfs            = sourceFiles.collect { case sf if packageSources(sf.fullPath.toString) => sf }

            PackageSummary(
              name,
              stms,
              invoked,
              mutants.filter(_.info.packageName == name),
              packageTests,
              sfs
            )
        }(collection.breakOut)

    ProjectSummary(
      statements,
      invokedStatements,
      mutants,
      packages
    )
  }
}
