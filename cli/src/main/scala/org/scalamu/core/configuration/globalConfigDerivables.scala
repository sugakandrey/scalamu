package org.scalamu.core.configuration

import java.io.File
import java.nio.file.Path

import com.typesafe.scalalogging.Logger
import org.scalamu.core.compilation.{IgnoreCoverageStatementsFilter, LoggingReporter}
import org.scalamu.plugin._

import scala.reflect.io.AbstractFile
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.Reporter

/**
 * Aggregates derivable instances, needed for the creation of [[org.scalamu.core.compilation.ScalamuGlobal]]
 */
trait GlobalDerivableInstances
    extends SettingsDerivable
    with ReporterDerivable
    with MutationConfigDerivable

trait SettingsDerivable {
  private def pathsToString: Traversable[Path] => String =
    _.foldLeft("")(_ + File.pathSeparator + _)

  implicit def settingsDerivable(implicit dir: AbstractFile): Derivable[Settings] =
    config =>
      new Settings {
        Yrangepos.value = true
        usejavacp.value = true
        sourcepath.value += pathsToString(config.sourceDirs)
        classpath.value += pathsToString(config.classPath)
        outputDirs.setSingleOutput(dir)
    }
}

trait ReporterDerivable extends SettingsDerivable {
  def log: Logger

  implicit def reporterDerivable(implicit dir: AbstractFile): Derivable[Reporter] =
    config => new LoggingReporter(log, config.derive[Settings])
}

trait MutationConfigDerivable {
  def guard: MutationGuard = FqnGuard(
    "org.scalamu.core.compilation.MutationGuard.enabledMutation"
  )

  implicit def mutationConfigDerivable(
    implicit reporter: MutationReporter
  ): Derivable[MutationConfig] =
    config =>
      MutationConfig(
        reporter,
        guard,
        IgnoreCoverageStatementsFilter(config.excludeSources),
        config.mutations
    )
}
