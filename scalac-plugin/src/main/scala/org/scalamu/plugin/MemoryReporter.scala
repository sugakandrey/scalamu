package org.scalamu.plugin

import scala.collection.mutable

class MemoryReporter extends MutationReporter {
  private val container =
    new mutable.HashMap[Int, mutable.Set[MutationInfo]] with mutable.MultiMap[Int, MutationInfo]

  def mutantsForRunId(runId: Int): Set[MutationInfo] =
    container.get(runId).fold(Set.empty[MutationInfo])(_.toSet)

  override def report(mutationInfo: MutationInfo): Unit =
    container.addBinding(mutationInfo.runId, mutationInfo)

  override def mutations: Set[MutationInfo] = container.get(1).fold(Set.empty[MutationInfo])(_.toSet)
}
