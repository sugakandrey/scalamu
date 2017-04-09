package org.scalamu.plugin.testutil

import org.scalamu.plugin.MutantInfo

import scala.tools.nsc.{Global, Settings}

trait MutationPhaseOnlyRunner extends PluginRunner with CompilationUtils {
  override def settings: Settings = new Settings {
    usejavacp.value = true
    stopAfter.value = List("mutating-transform")
  }
  override lazy val verifyTrees: Boolean              = false
  override lazy val sanitizeTrees: Boolean            = false
  override lazy val mutationReporter: TestingReporter = new TestingReporter

  def mutationsFor(
    code: String
  )(implicit global: Global): Seq[MutantInfo] =
    mutationReporter.mutationsForRunId(compile(code)).toSeq
}
