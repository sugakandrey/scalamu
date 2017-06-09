package org.scalamu.core.workers

import java.nio.file.Path

import org.scalamu.core.configuration.Derivable

import scala.util.matching.Regex

final case class CoverageWorkerConfig(
  testClassDirs: Set[Path],
  excludeTestsClasses: Seq[Regex],
  verbose: Boolean = false
)

object CoverageWorkerConfig {
  implicit val runnerConfigDerivable: Derivable[CoverageWorkerConfig] = config =>
    CoverageWorkerConfig(
      config.testClassDirs,
      config.excludeTestsClasses,
      config.verbose
    )
}
