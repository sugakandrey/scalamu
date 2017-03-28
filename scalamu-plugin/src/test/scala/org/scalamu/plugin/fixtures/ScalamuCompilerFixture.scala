package org.scalamu.plugin.fixtures

import org.scalamu.plugin.{PluginRunner, ScalamuPlugin}
import org.scalatest.{BeforeAndAfterAll, TestSuite}

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.Plugin

trait ScalamuCompilerFixture { self: PluginRunner =>
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
        new ScalamuPlugin(this, mutationReporter, mutations) :: super.loadRoughPluginsList()
    }

  override def withScalamuCompiler(
    testCode: Global => Any
  ): Any =
    testCode(global)
}

trait IsolatedScalamuCompilerFixture extends ScalamuCompilerFixture { self: PluginRunner =>
  override def withScalamuCompiler(
    testCode: Global => Any
  ): Any = {
    val global = new Global(settings, reporter) {
      override protected def loadRoughPluginsList(): List[Plugin] =
        new ScalamuPlugin(this, mutationReporter, mutations) :: super.loadRoughPluginsList()
    }

    testCode(global)
  }
}
