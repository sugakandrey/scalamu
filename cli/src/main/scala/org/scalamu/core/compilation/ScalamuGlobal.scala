package org.scalamu.core.compilation

import com.typesafe.scalalogging.Logger
import org.scalamu.core.SourceInfo
import org.scalamu.core.configuration.{GlobalDerivableInstances, ScalamuConfig}
import org.scalamu.core.coverage.{CoveragePlugin, InstrumentationReporter}
import org.scalamu.plugin.{Mutation, MutationConfig, MutationReporter, ScalamuPlugin}

import scala.collection.Set
import scala.reflect.internal.util.{BatchSourceFile, SourceFile}
import scala.reflect.io.{PlainFile, Path => ReflectPath}
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.{Global, Settings}

/**
 * [[scala.tools.nsc.Global]] wrapper, with Scoverage and Scalamu plugins
 * enabled and a couple of utility methods.
 */
class ScalamuGlobal private (
  settings: Settings,
  reporter: Reporter,
  mutationConfig: MutationConfig,
  mutations: Seq[Mutation],
  instrumentationReporter: InstrumentationReporter
) extends Global(settings, reporter) {

  private val coveragePlugin = new CoveragePlugin(this, instrumentationReporter)
  private val scalamuPlugin  = new ScalamuPlugin(this, mutations, mutationConfig)

  override protected def loadRoughPluginsList(): List[Plugin] =
    coveragePlugin :: scalamuPlugin :: super.loadRoughPluginsList()

  def withPhasesSkipped(phases: List[SkippablePhase]): ScalamuGlobal = {
    settings.skip.value = phases.map(_.name)
    this
  }

  def compile(suites: Set[SourceInfo]): Int = {
    val sourceFiles: List[SourceFile] = suites.map(
      suite =>
        new BatchSourceFile(
          new PlainFile(
            ReflectPath(suite.fullPath.toFile)
          )
      )
    )(collection.breakOut)
    val run = new Run
    val id  = currentRunId
    run.compileSources(sourceFiles)
    id
  }
}

object ScalamuGlobal extends GlobalDerivableInstances {
  override val log = Logger[ScalamuGlobal]

  def apply(
    config: ScalamuConfig,
    instrumentationReporter: InstrumentationReporter,
    mutationReporter: MutationReporter
  ): ScalamuGlobal = {
    implicit val reporter = mutationReporter
    new ScalamuGlobal(
      config.derive[Settings],
      config.derive[Reporter],
      config.derive[MutationConfig],
      config.mutations,
      instrumentationReporter
    )
  }
}
