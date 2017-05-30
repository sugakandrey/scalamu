package org.scalamu.core.compilation

import org.scalamu.core.coverage.InstrumentationReporter
import org.scalamu.plugin.{MemoryReporter, MutationConfig}
import org.scalamu.plugin.fixtures._
import org.scalamu.testutil.TestingInstrumentationReporter
import org.scalamu.testutil.fixtures.{IsolatedInstrumentationReporterFixture, SharedInstrumentationReporterFixture}
import org.scalatest.{BeforeAndAfterAll, Matchers, TestSuite}

import scala.reflect.io.{AbstractFile, VirtualDirectory}
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.ReplGlobal
import scala.tools.nsc.reporters.Reporter

trait ScalamuGlobalFixture extends TestSuite with Matchers {
  def createGlobal(
    settings: Settings,
    reporter: Reporter,
    outputDir: AbstractFile,
    config: MutationConfig,
    instrumentationReporter: InstrumentationReporter
  ): ScalamuGlobal =
    new ScalamuGlobal(settings, reporter, config, instrumentationReporter) with ReplGlobal
}

trait SharedScalamuGlobalFixture
    extends ScalamuGlobalFixture
    with SharedGlobalConfigFixture
    with SharedPluginConfigFixture
    with SharedInstrumentationReporterFixture
    with BeforeAndAfterAll {

  private[scalamu] var global: ScalamuGlobal = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    global = createGlobal(
      settings,
      reporter,
      outputDir,
      config,
      instrumentation
    )
  }

  def withScalamuCompiler(
    testCode: (ScalamuGlobal, MemoryReporter, TestingInstrumentationReporter) => Any
  ): Any = {
    testCode(global, config.reporter.asInstanceOf[MemoryReporter], instrumentation)
    global.reporter.hasErrors should ===(false)
    global.reporter.reset()
    global.settings.outputDirs.getSingleOutput match {
      case Some(vd: VirtualDirectory) => vd.clear()
      case _                          =>
    }
  }
}

trait IsolatedScalamuGlobalFixture
    extends ScalamuGlobalFixture
    with IsolatedGlobalConfigFixture
    with IsolatedPluginConfigFixture
    with IsolatedInstrumentationReporterFixture {

  def withScalamuGlobal(
    config: MutationConfig
  )(
    testCode: (ScalamuGlobal, MemoryReporter, TestingInstrumentationReporter) => Any
  ): Any = withGlobalConfig { (settings, reporter) =>
    withInstrumentationReporter { instrumentationReporter =>
      val global = createGlobal(
        settings,
        reporter,
        outputDir,
        config,
        instrumentationReporter
      )
      
      testCode(global, config.reporter.asInstanceOf[MemoryReporter], instrumentationReporter)
      reporter.hasErrors should ===(false)
      global.settings.outputDirs.getSingleOutput match {
        case Some(vd: VirtualDirectory) => vd.clear()
        case _                          =>
      }
    }
  }

  def withScalamuGlobal(
    testCode: (ScalamuGlobal, MemoryReporter, TestingInstrumentationReporter) => Any
  ): Any = withPluginConfig { config =>
    withScalamuGlobal(config)(testCode)
  }
}
