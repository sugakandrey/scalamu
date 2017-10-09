package org.scalamu.plugin.testutil

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture

trait SingleMutationSpec extends MutationPhaseRunner with SharedScalamuCompilerFixture with CompilationUtils {

  def mutation: Mutator
  override final def mutations: Seq[Mutator] = List(mutation)
}
