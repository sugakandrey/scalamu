package org.scalamu.plugin

import scala.collection.mutable

class MemoryReporter extends MutationReporter {
  private val container =
    new mutable.HashMap[Int, mutable.Set[MutantInfo]] with mutable.MultiMap[Int, MutantInfo]

  def mutantsForRunId(runId: Int): Set[MutantInfo] =
    container.get(runId).fold(Set.empty[MutantInfo])(_.toSet)

  override def report(mutantInfo: MutantInfo): Unit =
    container.addBinding(mutantInfo.runId, mutantInfo)

  override def mutants: Set[MutantInfo] = container(0).toSet
}
