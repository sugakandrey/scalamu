package org.scalamu.plugin.fixtures

import org.scalamu.plugin._
import org.scalatest.{BeforeAndAfterAll, TestSuite}

trait PluginConfigFixture extends TestSuite {
  def mutationReporter: MutationReporter
  def guard: MutationGuard
  def verifyTrees: Boolean
  def sanitizeTrees: Boolean
  def filter: MutationFilter

  def withPluginConfig(
    code: MutationConfig => Any
  ): Any
}

trait IsolatedPluginConfigFixture extends PluginConfigFixture {
  override def withPluginConfig(
    code: (MutationConfig) => Any
  ): Any = code(
    MutationConfig(
      mutationReporter,
      guard,
      filter,
      sanitizeTrees = sanitizeTrees,
      verifyTrees = verifyTrees
    )
  )
}

trait SharedPluginConfigFixture extends PluginConfigFixture with BeforeAndAfterAll {
  private[fixtures] var config: MutationConfig = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    config = MutationConfig(
      mutationReporter,
      guard,
      filter,
      sanitizeTrees = sanitizeTrees,
      verifyTrees = verifyTrees
    )
  }

  override def withPluginConfig(
    code: (MutationConfig) => Any
  ): Any = code(config)
}
