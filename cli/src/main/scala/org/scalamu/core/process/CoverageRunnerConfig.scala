package org.scalamu.core.process

import java.nio.file.Path

import org.scalamu.core.configuration.Derivable

import scala.util.matching.Regex

final case class CoverageRunnerConfig(
  testClassDirs: Set[Path],
  excludeTestsClasses: Seq[Regex],
  verbose: Boolean = false
)

object CoverageRunnerConfig {
  implicit val runnerConfigDerivable: Derivable[CoverageRunnerConfig] = config =>
    CoverageRunnerConfig(
      config.testClassDirs,
      config.excludeTestsClasses,
      config.verbose
    )
}
