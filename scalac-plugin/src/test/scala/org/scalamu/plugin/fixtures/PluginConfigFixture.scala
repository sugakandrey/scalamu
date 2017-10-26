package org.scalamu.plugin.fixtures

import org.scalamu.common.filtering.NameFilter
import org.scalamu.plugin._
import org.scalatest.{BeforeAndAfterAll, TestSuite}

trait PluginConfigFixture extends TestSuite {
  def mutationReporter: MemoryReporter
  def guard: MutationGuard
  def mutators: Seq[Mutator]
  def verifyTrees: Boolean
  def sanitizeTrees: Boolean
  def filter: NameFilter

  def withPluginConfig(
    code: ScalamuScalacConfig => Any
  ): Any
}

trait IsolatedPluginConfigFixture extends PluginConfigFixture {
  override def withPluginConfig(
    code: (ScalamuScalacConfig) => Any
  ): Any = code(
    ScalamuScalacConfig(
      mutationReporter,
      guard,
      filter,
      mutators,
      sanitizeTrees = sanitizeTrees,
      verifyTrees = verifyTrees
    )
  )
}

trait SharedPluginConfigFixture extends PluginConfigFixture with BeforeAndAfterAll {
  private[scalamu] var config: ScalamuScalacConfig = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    config = ScalamuScalacConfig(
      mutationReporter,
      guard,
      filter,
      mutators,
      sanitizeTrees = sanitizeTrees,
      verifyTrees = verifyTrees
    )
  }

  override def withPluginConfig(
    code: (ScalamuScalacConfig) => Any
  ): Any = code(config)
}
