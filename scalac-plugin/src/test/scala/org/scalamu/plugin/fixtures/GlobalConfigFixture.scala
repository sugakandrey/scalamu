package org.scalamu.plugin.fixtures

import org.scalatest.{BeforeAndAfterAll, TestSuite}

import scala.reflect.io.AbstractFile
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.Reporter

trait GlobalConfigFixture extends TestSuite {
  def outputDir: AbstractFile
  def createSettings(): Settings
  def createReporter(settings: Settings): Reporter

  def withGlobalConfig(
    testCode: (Settings, Reporter) => Any
  ): Any
}

trait IsolatedGlobalConfigFixture extends GlobalConfigFixture {
  override def withGlobalConfig(
    testCode: (Settings, Reporter) => Any
  ): Any = {
    val settings = createSettings()
    val reporter = createReporter(settings)
    testCode(settings, reporter)
  }
}

trait SharedGlobalConfigFixture extends GlobalConfigFixture with BeforeAndAfterAll {
  private[scalamu] var settings: Settings = _
  private[scalamu] var reporter: Reporter = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    settings = createSettings()
    reporter = createReporter(settings)
  }

  override def withGlobalConfig(
    testCode: (Settings, Reporter) => Any
  ): Any = testCode(settings, reporter)
}
