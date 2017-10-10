package org.scalamu.core
package configuration

import java.io.File

import com.typesafe.scalalogging.Logger
import org.scalamu.common.filtering.RegexFilter
import org.scalamu.core.compilation.{IgnoreCoverageStatementsFilter, LoggingReporter}
import org.scalamu.plugin._

import scala.reflect.io.AbstractFile
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.Reporter
import scala.util.Properties

/**
 * Aggregates derivable instances, needed for the creation of [[org.scalamu.core.compilation.ScalamuGlobal]]
 */
trait GlobalDerivableInstances extends SettingsDerivable with ReporterDerivable with MutationConfigDerivable

trait SettingsDerivable {
  def log: Logger

  implicit def settingsDerivable(implicit dir: AbstractFile): Derivable[Settings] =
    config => {
      val settings = new Settings {
        Yrangepos.value = true
        classpath.value  += File.pathSeparator + pathsToString(config.classPath)
        classpath.value  += Properties.javaClassPath
        sourcepath.value += pathsToString(config.sourceDirs)
        outputDirs.setSingleOutput(dir)
      }

      val (success, unprocessed) = settings.processArgumentString(config.scalacParameters)

      if (!success) {
        log.error(s"Bad scalac options in ${config.scalacParameters}.")
        die(InternalFailure)
      }

      if (unprocessed.nonEmpty) {
        log.debug(s"Unprocessed scalac options: ${unprocessed.mkString("[", " , ", "]")}")
      }

      settings
    }
}

trait ReporterDerivable extends SettingsDerivable {
  def log: Logger

  implicit def reporterDerivable(implicit dir: AbstractFile): Derivable[Reporter] =
    config => new LoggingReporter(log, config.derive[Settings])
}

trait MutationConfigDerivable {
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
        IgnoreCoverageStatementsFilter,
        config.activeMutators,
        RegexFilter(config.targetClasses: _*)
    )
}
