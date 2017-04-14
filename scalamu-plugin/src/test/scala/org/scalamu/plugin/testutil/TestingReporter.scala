package org.scalamu.plugin.testutil

import org.scalamu.plugin.{MutantInfo, MutationReporter}

import scala.collection.mutable

class TestingReporter extends MutationReporter {
  private val mutations =
    new mutable.HashMap[Int, mutable.Set[MutantInfo]] with mutable.MultiMap[Int, MutantInfo]

  def mutantsForRunId(runId: Int): Set[MutantInfo] =
    mutations.get(runId).fold(Set.empty[MutantInfo])(_.toSet)

  override def report(mutationInfo: MutantInfo): Unit =
    mutations.addBinding(mutationInfo.runId, mutationInfo)
}
