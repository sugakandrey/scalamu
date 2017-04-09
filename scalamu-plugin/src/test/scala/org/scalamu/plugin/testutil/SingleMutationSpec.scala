package org.scalamu.plugin.testutil

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalatest.{FlatSpec, Matchers}

trait SingleMutationSpec
    extends FlatSpec
    with Matchers
    with MutationPhaseOnlyRunner
    with CompilationUtils
    with SharedScalamuCompilerFixture {

  def mutation: Mutation
  override final def mutations: Seq[Mutation] = List(mutation)
}
