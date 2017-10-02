package org.scalamu.report

import org.scalamu.core.{SourceInfo, TestedMutant}
import org.scalamu.core.coverage.Statement
import org.scalamu.testapi.AbstractTestSuite

final case class ProjectSummary private (
  override val statements: Set[Statement],
  override val invokedStatements: Set[Statement],
  override val mutants: Set[TestedMutant],
  tests: Set[AbstractTestSuite],
  packages: Set[PackageSummary]
) extends CoverageStats {

  def sourceFiles: Int = packages.map(_.sourceFiles.size).sum
  def loc: Int =
    (for {
      aPackage   <- packages
      sourceFile <- aPackage.sourceFiles
    } yield sourceFile.lines.size).sum
}

object ProjectSummaryFactory {
  def apply(
    statements: Set[Statement],
    invokedStatements: Set[Statement],
    mutants: Set[TestedMutant],
    sourceFiles: Set[SourceInfo],
    tests: Set[AbstractTestSuite]
  ): ProjectSummary = {
    val packages: Set[PackageSummary] =
      statements
        .groupBy(_.location.packageName)
        .map {
          case (name, stms) =>
            val invoked        = invokedStatements.filter(_.location.packageName == name)
            val packageSources = stms.map(_.location.sourcePath)
            val sfs            = sourceFiles.collect { case sf if packageSources(sf.fullPath.toString) => sf }

            PackageSummary(
              name,
              stms,
              invoked,
              mutants.filter(_.info.packageName == name),
              Set.empty,
              sfs
            )
        }(collection.breakOut)

    ProjectSummary(
      statements,
      invokedStatements,
      mutants,
      tests,
      packages
    )
  }
}
