package org.scalamu.plugin.fixtures

import org.scalamu.common.filtering.NameFilter
import org.scalamu.plugin._
import org.scalamu.plugin.testutil.TestingReporter
import org.scalatest.{BeforeAndAfterAll, TestSuite}

trait PluginConfigFixture extends TestSuite {
  def mutationReporter: TestingReporter
  def guard: MutationGuard
  def mutations: Seq[Mutation]
  def verifyTrees: Boolean
  def sanitizeTrees: Boolean
  def filter: NameFilter

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
      mutations,
      sanitizeTrees = sanitizeTrees,
      verifyTrees = verifyTrees
    )
  )
}

trait SharedPluginConfigFixture
    extends PluginConfigFixture
    with BeforeAndAfterAll {
  private[scalamu] var config: MutationConfig = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    config = MutationConfig(
      mutationReporter,
      guard,
      filter,
      mutations,
      sanitizeTrees = sanitizeTrees,
      verifyTrees = verifyTrees
    )
  }

  override def withPluginConfig(
    code: (MutationConfig) => Any
  ): Any = code(config)
}
