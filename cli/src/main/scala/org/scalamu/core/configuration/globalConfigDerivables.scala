package org.scalamu.core.configuration

import java.io.File
import java.nio.file.Path

import com.typesafe.scalalogging.Logger
import org.scalamu.core.compilation.{IgnoreCoverageStatementsFilter, LoggingReporter}
import org.scalamu.plugin._

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

  implicit val settingsDerivable: Derivable[Settings] = config =>
    new Settings {
      Yrangepos.value = true
      usejavacp.value = true
      sourcepath.value += pathsToString(config.sourceDirs)
      classpath.value += pathsToString(config.classPath)
  }
}

trait ReporterDerivable extends SettingsDerivable {
  def log: Logger

  implicit val reporterDerivable: Derivable[Reporter] = config =>
    new LoggingReporter(log, config.derive[Settings])
}

trait MutationConfigDerivable {
  def guard: MutationGuard = FqnPrefixedGuard(ScalamuPluginConfig.mutationGuardPrefix)

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
