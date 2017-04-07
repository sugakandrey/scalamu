package org.scalamu.plugin.util

import org.scalamu.plugin._

import scala.collection.{mutable, Set}
import scala.reflect.internal.util.SourceFile
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.{Global, Settings}

trait PluginRunner {
  def mutations: Seq[Mutation]
  val mutationReporter: MutationReporter
  def settings: Settings
  val reporter: Reporter
  val guard: MutationGuard
  val verifyTrees: Boolean
  val sanitizeTrees: Boolean

  protected def compileFiles(files: Seq[SourceFile])(implicit global: Global): Int = {
    val run = new global.Run
    val id  = global.currentRunId
    run.compileSources(files.toList)
    id
  }

  class TestingReporter extends MutationReporter {
    private val mutations =
      new mutable.HashMap[Int, mutable.Set[MutationInfo]] with mutable.MultiMap[Int, MutationInfo]

    def mutationsForRunId(runId: Int): Set[MutationInfo] = mutations.getOrElse(runId, Set.empty)

    override def report(mutationInfo: MutationInfo): Unit =
      mutations.addBinding(mutationInfo.runId, mutationInfo)
  }
}
