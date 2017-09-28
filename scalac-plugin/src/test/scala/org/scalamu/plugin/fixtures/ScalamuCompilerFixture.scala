package org.scalamu.plugin.fixtures

import org.scalamu.plugin.{MemoryReporter, Mutator, ScalamuScalacConfig, ScalamuPlugin}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers._

import scala.reflect.io.{AbstractFile, VirtualDirectory}
import scala.tools.nsc.interpreter.ReplGlobal
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.{Global, Settings}

/**
 * Preferred way of testing scalamu compiler plugin.
 */
trait ScalamuCompilerFixture {
  def createGlobal(
    settings: Settings,
    reporter: Reporter,
    outputDir: AbstractFile,
    config: ScalamuScalacConfig
  ): Global = new Global(settings, reporter) with ReplGlobal {
    override protected def loadRoughPluginsList(): List[Plugin] =
      new ScalamuPlugin(this, config) :: super.loadRoughPluginsList()
  }
}

trait SharedScalamuCompilerFixture
    extends ScalamuCompilerFixture
    with SharedGlobalConfigFixture
    with SharedPluginConfigFixture
    with BeforeAndAfterAll {

  private[fixtures] var global: Global = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    global = createGlobal(
      settings,
      reporter,
      outputDir,
      config
    )
  }

  def withScalamuCompiler(testCode: (Global, MemoryReporter) => Any): Any = {
    testCode(global, config.reporter.asInstanceOf[MemoryReporter])
    global.reporter.hasErrors should ===(false)
    global.reporter.reset()
    global.settings.outputDirs.getSingleOutput match {
      case Some(vd: VirtualDirectory) => vd.clear()
      case _                          =>
    }
  }
}

trait IsolatedScalamuCompilerFixture
    extends ScalamuCompilerFixture
    with IsolatedGlobalConfigFixture
    with IsolatedPluginConfigFixture {

  def withScalamuCompiler(
                           mutations: Seq[Mutator],
                           config: ScalamuScalacConfig
  )(
    testCode: (Global, MemoryReporter) => Any
  ): Any = withGlobalConfig { (settings, reporter) =>
    val global = createGlobal(
      settings,
      reporter,
      outputDir,
      config.copy(mutators = mutations)
    )

    testCode(global, config.reporter.asInstanceOf[MemoryReporter])
    reporter.hasErrors should ===(false)
    global.settings.outputDirs.getSingleOutput match {
      case Some(vd: VirtualDirectory) => vd.clear()
      case _                          =>
    }
  }

  def withScalamuCompiler(
    testCode: (Global, MemoryReporter) => Any
  ): Any = withPluginConfig { config =>
    withScalamuCompiler(mutations, config)(testCode)
  }
}
