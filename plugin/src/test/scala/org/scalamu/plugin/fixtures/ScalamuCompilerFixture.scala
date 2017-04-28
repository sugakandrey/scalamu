package org.scalamu.plugin.fixtures

import org.scalamu.plugin.testutil.TestingReporter
import org.scalamu.plugin.{Mutation, MutationConfig, ScalamuPlugin}
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
    mutations: Seq[Mutation],
    config: MutationConfig
  ): Global = new Global(settings, reporter) with ReplGlobal {
    override protected def loadRoughPluginsList(): List[Plugin] =
      new ScalamuPlugin(this, mutations, config) :: super.loadRoughPluginsList()
  }
}

trait SharedScalamuCompilerFixture
    extends ScalamuCompilerFixture
    with SharedGlobalConfigFixture
    with SharedPluginConfigFixture
    with SharedMutationsFixture
    with BeforeAndAfterAll {

  private[fixtures] var global: Global = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    global = createGlobal(
      settings,
      reporter,
      outputDir,
      availableMutations,
      config
    )
  }

  def withScalamuCompiler(testCode: (Global, TestingReporter) => Any): Any = {
    testCode(global, mutationReporter)
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
    with IsolatedMutationsFixture
    with IsolatedPluginConfigFixture {

  def withScalamuCompiler(
    mutations: Seq[Mutation],
    config: MutationConfig
  )(
    testCode: (Global, TestingReporter) => Any
  ): Any = withGlobalConfig { (settings, reporter) =>
    val global = createGlobal(
      settings,
      reporter,
      outputDir,
      mutations,
      config
    )

    testCode(global, mutationReporter)
    reporter.hasErrors should ===(false)
    global.settings.outputDirs.getSingleOutput match {
      case Some(vd: VirtualDirectory) => vd.clear()
      case _                          =>
    }
  }

  def withScalamuCompiler(
    testCode: (Global, TestingReporter) => Any
  ): Any = withMutations { mutations =>
    withPluginConfig { config =>
      withScalamuCompiler(mutations, config)(testCode)
    }
  }
}
