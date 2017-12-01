package org.scalamu.core.coverage

import org.scalamu.common.filtering.RegexFilter
import org.scalamu.core.compilation.{IgnoreCoverageStatementsFilter, LoggingReporter}
import org.scalamu.core.configuration.Derivable
import org.scalamu.plugin._
import scribe.Logging

import scala.reflect.internal.Reporter
import scala.tools.nsc.Settings

trait TestingDerivableInstance extends Logging {
  def mutationReporter: MutationReporter
  def settings: Settings
  def guard: MutationGuard = FqnGuard(ScalamuPluginConfig.mutationGuardPrefix)

  implicit val settingsDerivable: Derivable[Settings] = Function.const(settings)

  implicit val reporterDerivable: Derivable[Reporter] =
    Function.const(new LoggingReporter(logger, settings))

  implicit val mutationConfigDerivable: Derivable[ScalamuScalacConfig] =
    config =>
      ScalamuScalacConfig(
        mutationReporter,
        guard,
        IgnoreCoverageStatementsFilter,
        targetOwners = RegexFilter(config.targetOwners: _*)
    )
}
