package org.scalamu.plugin.testutil

import org.scalamu.plugin._
import org.scalamu.plugin.AcceptAllFilter

import scala.collection.{mutable, Set}
import scala.reflect.internal.util.SourceFile
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}
import scala.tools.nsc.{Global, Settings}

trait PluginRunner {
  def mutations: Seq[Mutation]
  def settings: Settings
  lazy val mutationReporter: MutationReporter = new TestingReporter
  lazy val reporter: Reporter                 = new ConsoleReporter(settings)
  lazy val guard: MutationGuard               = NoOpGuard
  lazy val verifyTrees: Boolean               = true
  lazy val sanitizeTrees: Boolean             = true
  lazy val filter: MutationFilter             = AcceptAllFilter

  protected def compileFiles(files: Seq[SourceFile])(implicit global: Global): Int = {
    val run = new global.Run
    val id  = global.currentRunId
    run.compileSources(files.toList)
    id
  }

  class TestingReporter extends MutationReporter {
    private val mutations =
      new mutable.HashMap[Int, mutable.Set[MutantInfo]] with mutable.MultiMap[Int, MutantInfo]

    def mutationsForRunId(runId: Int): Set[MutantInfo] = mutations.getOrElse(runId, Set.empty)

    override def report(mutationInfo: MutantInfo): Unit =
      mutations.addBinding(mutationInfo.runId, mutationInfo)
  }
}
