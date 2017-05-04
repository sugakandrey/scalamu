package org.scalamu.core.compilation

import com.typesafe.scalalogging.Logger
import org.scalamu.core.SourceInfo
import org.scalamu.core.configuration.{GlobalDerivableInstances, ScalamuConfig}
import org.scalamu.core.coverage.{CoveragePlugin, InstrumentationReporter}
import org.scalamu.plugin.{MutationConfig, MutationReporter, ScalamuPlugin}

import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.io.{AbstractFile, PlainFile, Path => ReflectPath}
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.{Global, Settings}

/**
 * [[scala.tools.nsc.Global]] wrapper, with Scoverage and Scalamu plugins
 * enabled and a couple of utility methods.
 */
class ScalamuGlobal private[compilation] (
  settings: Settings,
  reporter: Reporter,
  mutationConfig: MutationConfig,
  instrumentationReporter: InstrumentationReporter
) extends Global(settings, reporter) {
  require(
    settings.outputDirs.getSingleOutput.isDefined,
    "Single output dir must be set for ScalamuGlobal"
  )

  private val coveragePlugin = new CoveragePlugin(this, instrumentationReporter)
  private val scalamuPlugin  = new ScalamuPlugin(this, mutationConfig)

  override protected def loadRoughPluginsList(): List[Plugin] =
    coveragePlugin :: scalamuPlugin :: super.loadRoughPluginsList()

  def outputDir(): AbstractFile = settings.outputDirs.getSingleOutput.get

  def withPhasesSkipped(phases: PluginPhase*): ScalamuGlobal = {
    settings.skip.value = phases.map(_.name)(collection.breakOut)
    this
  }

  def compile(suites: List[SourceInfo]): Int = {
    val sourceFiles = suites.map(
      suite =>
        new BatchSourceFile(
          new PlainFile(
            ReflectPath(suite.fullPath.toFile)
          )
      )
    )
    ScalamuGlobal.log.debug(s"Compiling source files: $sourceFiles")
    val run = new Run
    val id  = currentRunId
    run.compileSources(sourceFiles)
    id
  }
}

object ScalamuGlobal extends GlobalDerivableInstances {
  override val log: Logger = Logger[ScalamuGlobal]

  def apply(
    config: ScalamuConfig,
    instrumentationReporter: InstrumentationReporter,
    mutationReporter: MutationReporter,
    outputDir: AbstractFile
  ): ScalamuGlobal = {
    implicit val reporter = mutationReporter
    implicit val dir      = outputDir
    new ScalamuGlobal(
      config.derive[Settings],
      config.derive[Reporter],
      config.derive[MutationConfig],
      instrumentationReporter
    )
  }
}
