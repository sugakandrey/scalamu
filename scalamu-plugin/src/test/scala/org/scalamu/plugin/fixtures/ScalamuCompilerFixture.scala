package org.scalamu.plugin.fixtures

import org.scalamu.plugin.{MutationConfig, ScalamuPlugin}
import org.scalamu.plugin.util.PluginRunner
import org.scalatest.{BeforeAndAfterAll, TestSuite}
import org.scalatest.Matchers._

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.Plugin

trait ScalamuCompilerFixture { self: PluginRunner =>
  protected val config = MutationConfig(mutationReporter, guard, verifyTrees = true)
  
  def withScalamuCompiler(
    testCode: Global => Any
  ): Any
}

trait SharedScalamuCompilerFixture
    extends ScalamuCompilerFixture
    with TestSuite
    with BeforeAndAfterAll { self: PluginRunner =>

  private var global: Global = _

  override protected def beforeAll(): Unit =
    global = new Global(settings, reporter) {
      override protected def loadRoughPluginsList(): List[Plugin] =
        new ScalamuPlugin(this, mutations, config) :: super.loadRoughPluginsList()
    }

  override def withScalamuCompiler(
    testCode: Global => Any
  ): Any = {
    testCode(global)
    reporter.hasErrors should ===(false)
  }
}

trait IsolatedScalamuCompilerFixture extends ScalamuCompilerFixture { self: PluginRunner =>
  override def withScalamuCompiler(
    testCode: Global => Any
  ): Any = {
    val global = new Global(settings, reporter) {
      override protected def loadRoughPluginsList(): List[Plugin] =
        new ScalamuPlugin(this, mutations, config) :: super.loadRoughPluginsList()
    }

    testCode(global)
    reporter.hasErrors should ===(false)
  }
}
