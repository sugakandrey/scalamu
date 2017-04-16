package org.scalamu.plugin.testutil

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture

trait SingleMutationSpec
    extends MutationPhaseRunner
    with SharedScalamuCompilerFixture
    with CompilationUtils {

  def mutation: Mutation
  override final def mutations: Seq[Mutation] = List(mutation)
}
