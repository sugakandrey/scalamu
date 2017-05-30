package org.scalamu.core.coverage

import com.typesafe.scalalogging.Logger
import org.scalamu.core.compilation.{IgnoreCoverageStatementsFilter, LoggingReporter}
import org.scalamu.core.configuration.Derivable
import org.scalamu.plugin._

import scala.reflect.internal.Reporter
import scala.tools.nsc.Settings

trait TestingDerivableInstance {
  def mutationReporter: MutationReporter
  def settings: Settings
  def log: Logger
  def guard: MutationGuard = FqnGuard(ScalamuPluginConfig.mutationGuardPrefix)

  implicit val settingsDerivable: Derivable[Settings] = Function.const(settings)

  implicit val reporterDerivable: Derivable[Reporter] =
    Function.const(new LoggingReporter(log, settings))

  implicit val mutationConfigDerivable: Derivable[MutationConfig] =
    config =>
      MutationConfig(
        mutationReporter,
        guard,
        IgnoreCoverageStatementsFilter(config.excludeSources)
    )
}
