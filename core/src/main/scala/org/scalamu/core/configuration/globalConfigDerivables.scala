package org.scalamu.core
package configuration

import java.io.File

import com.typesafe.scalalogging.Logger
import org.scalamu.common.filtering.{CompositeNameFilter, InverseRegexFilter, RegexFilter}
import org.scalamu.core.api.InternalFailure
import org.scalamu.core.compilation.{IgnoreCoverageStatementsFilter, LoggingReporter}
import org.scalamu.plugin._

import scala.reflect.io.AbstractFile
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.Reporter
import scala.util.Properties

/**
 * Aggregates derivable instances, needed for the creation of [[org.scalamu.core.compilation.ScalamuGlobal]]
 */
trait GlobalDerivableInstances extends SettingsDerivable with ReporterDerivable with ScalamuScalacPluginConfigDerivable

trait SettingsDerivable {
  def log: Logger

  implicit def settingsDerivable(implicit dir: AbstractFile): Derivable[Settings] =
    config => {
      val settings = new Settings {
        Yrangepos.value     = true
        Ycompacttrees.value = true
        classpath.value  += File.pathSeparator + concatPaths(config.classPath)
        classpath.value  += Properties.javaClassPath
        sourcepath.value += concatPaths(config.sourceDirs)
        outputDirs.setSingleOutput(dir)
      }

      val (success, unprocessed) = settings.processArgumentString(config.scalacParameters)

      if (!success) {
        log.error(s"Bad scalac options in ${config.scalacParameters}.")
        die(InternalFailure)
      }

      if (unprocessed.nonEmpty) {
        log.warn(s"Unprocessed scalac options: ${unprocessed.mkString("[", " , ", "]")}")
      }

      settings
    }
}

trait ReporterDerivable extends SettingsDerivable {
  def log: Logger

  implicit def reporterDerivable(implicit dir: AbstractFile): Derivable[Reporter] =
    config => new LoggingReporter(log, config.derive[Settings])
}

trait ScalamuScalacPluginConfigDerivable {
  def guard: MutationGuard = FqnGuard(
    "org.scalamu.compilation.MutationGuard.enabledMutation"
  )

  implicit def mutationConfigDerivable(
    implicit reporter: MutationReporter
  ): Derivable[ScalamuScalacConfig] =
    config =>
      ScalamuScalacConfig(
        reporter,
        guard,
        new CompositeNameFilter(
          InverseRegexFilter(config.ignoreSymbols: _*),
          IgnoreCoverageStatementsFilter,
        ),
        config.activeMutators,
        RegexFilter(config.targetOwners: _*)
    )
}
