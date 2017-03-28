package org.scalamu.plugin

import scala.collection.{mutable, Set}
import scala.reflect.internal.util.{BatchSourceFile, NoFile}
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}
import scala.tools.nsc.{Global, Settings}

trait PluginRunner {
  def mutations: Seq[Mutation]
  val mutationReporter: MutationReporter
  def settings: Settings
  def reporter: Reporter

  protected def compile(
    code: String
  )(implicit global: Global): Int = {
    val file = new BatchSourceFile(NoFile, code)
    val run  = new global.Run
    val id   = global.currentRunId
    run.compileSources(List(file))
    id
  }
}

trait MutationOnlyRunner extends PluginRunner {
  override val mutationReporter: TestingReporter = new TestingReporter
  override def settings: Settings = new Settings {
    usejavacp.value = true
    stopAfter.value = List("mutating-transform")
  }
  override def reporter: Reporter = new ConsoleReporter(settings)

   def mutationsFor(
    code: String
  )(implicit
    global: Global
  ): Seq[MutationInfo] =
    mutationReporter.mutationsForRunId(compile(code)).toSeq
}

class TestingReporter extends MutationReporter {
  private val mutations =
    new mutable.HashMap[Int, mutable.Set[MutationInfo]] with mutable.MultiMap[Int, MutationInfo]

  def mutationsForRunId(runId: Int): Set[MutationInfo] = mutations.getOrElse(runId, Set.empty)

  override def report(mutationInfo: MutationInfo): Unit =
    mutations.addBinding(mutationInfo.runId, mutationInfo)
}
