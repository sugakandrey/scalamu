package org.scalamu.plugin.util

import org.scalamu.plugin.{MutationGuard, MutationInfo, NoOpMutationGuard}

import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}
import scala.tools.nsc.{Global, Settings}

trait MutationPhaseOnlyRunner extends PluginRunner with CompilationUtils {
  override val mutationReporter: TestingReporter = new TestingReporter
  override def settings: Settings = new Settings {
    usejavacp.value = true
    stopAfter.value = List("mutating-transform")
  }
  override def reporter: Reporter   = new ConsoleReporter(settings)
  override def guard: MutationGuard = NoOpMutationGuard

  def mutationsFor(
    code: String
  )(implicit global: Global): Seq[MutationInfo] =
    mutationReporter.mutationsForRunId(compile(code)).toSeq
}
