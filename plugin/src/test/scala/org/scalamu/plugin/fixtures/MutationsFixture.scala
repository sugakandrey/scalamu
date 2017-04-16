package org.scalamu.plugin.fixtures

import org.scalamu.plugin.Mutation
import org.scalatest.{BeforeAndAfterAll, TestSuite}

trait MutationsFixture extends TestSuite {
  def mutations: Seq[Mutation]

  def withMutations(
    code: Seq[Mutation] => Any
  ): Any
}

trait SharedMutationsFixture extends MutationsFixture with BeforeAndAfterAll {
  private[fixtures] var availableMutations: Seq[Mutation] = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    availableMutations = mutations
  }

  override def withMutations(
    code: Seq[Mutation] => Any
  ): Any = code(availableMutations)
}

trait IsolatedMutationsFixture extends MutationsFixture {
  override def withMutations(
    code: Seq[Mutation] => Any
  ): Any = code(mutations)
}
