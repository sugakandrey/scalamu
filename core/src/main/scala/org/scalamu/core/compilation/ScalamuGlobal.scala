package org.scalamu.core.compilation

import com.typesafe.scalalogging.Logger
import org.scalamu.core.api.SourceInfo
import org.scalamu.core.configuration.{GlobalDerivableInstances, ScalamuConfig}
import org.scalamu.core.coverage.{CoveragePlugin, InstrumentationReporter}
import org.scalamu.plugin.{MutationReporter, ScalamuPlugin, ScalamuScalacConfig}

import scala.reflect.io.AbstractFile
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
  scalacPluginConfig: ScalamuScalacConfig,
  instrumentationReporter: InstrumentationReporter
) extends Global(settings, reporter) {
  ScalamuGlobal.log.debug(s"Initialised ScalamuGlobal with classpath: ${settings.classpath}.")

  require(
    settings.outputDirs.getSingleOutput.isDefined,
    "Single output dir must be set for ScalamuGlobal"
  )

  private val coveragePlugin = new CoveragePlugin(this, instrumentationReporter)
  private val scalamuPlugin  = new ScalamuPlugin(this, scalacPluginConfig)

  override protected def loadRoughPluginsList(): List[Plugin] =
    coveragePlugin :: scalamuPlugin :: super.loadRoughPluginsList()

  def outputDir: AbstractFile = settings.outputDirs.getSingleOutput.get

  def withPhasesSkipped(phases: PluginPhase*): ScalamuGlobal = {
    settings.skip.value = phases.map(_.name)(collection.breakOut)
    this
  }

  def compile(suites: List[SourceInfo]): Int = {
    val sourceFiles = suites.map(suite => getSourceFile(suite.fullPath.toString))
    ScalamuGlobal.log.info(s"Compiling ${sourceFiles.size} source files.")
    ScalamuGlobal.log.debug(s"Source files: ${sourceFiles.mkString("[\n\t", "\n\t", "\n]")}")
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
    implicit val reporter: MutationReporter = mutationReporter
    implicit val dir: AbstractFile          = outputDir

    new ScalamuGlobal(
      config.derive[Settings],
      config.derive[Reporter],
      config.derive[ScalamuScalacConfig],
      instrumentationReporter
    )
  }
}
