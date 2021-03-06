package org.scalamu.core.process

import java.nio.file.Path

import org.scalamu.core.configuration.Derivable

import scala.util.matching.Regex

final case class CoverageProcessConfig(
  testClassDirs: Set[Path],
  targetTests: Seq[Regex],
  testingOptions: Map[String, String],
  verbose: Boolean = false
)

object CoverageProcessConfig {
  implicit val runnerConfigDerivable: Derivable[CoverageProcessConfig] = config =>
    CoverageProcessConfig(
      config.testClassDirs,
      config.targetTests,
      config.testingOptions,
      config.verbose
  )
}
