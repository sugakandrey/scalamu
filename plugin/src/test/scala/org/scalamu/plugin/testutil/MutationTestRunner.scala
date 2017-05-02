package org.scalamu.plugin.testutil

import org.scalamu.common.filtering.{AcceptAllFilter, NameFilter}
import org.scalamu.plugin._
import org.scalamu.plugin.fixtures._
import org.scalatest.{FlatSpec, Matchers}

import scala.reflect.io._
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}

trait MutationTestRunner
    extends FlatSpec
    with Matchers
    with GlobalConfigFixture
    with PluginConfigFixture
    with CompilationUtils {

  override def outputDir: AbstractFile = new VirtualDirectory("[memory]", None)

  override def createSettings(): Settings = new Settings {
    usejavacp.value = true
    outputDirs.setSingleOutput(outputDir)
  }
  override def createReporter(settings: Settings): Reporter = new ConsoleReporter(settings)

  override def mutationReporter: TestingReporter = new TestingReporter
  override val guard: MutationGuard              = NoOpGuard
  override val filter: NameFilter            = AcceptAllFilter
  override val sanitizeTrees: Boolean            = false
  override val verifyTrees: Boolean              = false
}

trait MutationPhaseRunner extends MutationTestRunner {
  override def createSettings(): Settings = new Settings {
    usejavacp.value = true
    outputDirs.setSingleOutput(outputDir)
    stopAfter.value = List("mutating-transform")
  }
}
