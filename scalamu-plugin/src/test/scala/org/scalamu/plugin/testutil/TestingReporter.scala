package org.scalamu.plugin.testutil

import org.scalamu.plugin.{MutantInfo, MutationReporter}

import scala.collection.mutable

class TestingReporter extends MutationReporter {
  private val mutants =
    new mutable.HashMap[Int, mutable.Set[MutantInfo]] with mutable.MultiMap[Int, MutantInfo]

  def mutantsForRunId(runId: Int): Set[MutantInfo] =
    mutants.get(runId).fold(Set.empty[MutantInfo])(_.toSet)

  override def report(mutantInfo: MutantInfo): Unit =
    mutants.addBinding(mutantInfo.runId, mutantInfo)
}
