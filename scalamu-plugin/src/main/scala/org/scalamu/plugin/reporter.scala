package org.scalamu.plugin

import scala.reflect.internal.util.Position

trait MutationReporter {
  def report(mutationInfo: MutationInfo): Unit
}

final case class MutationInfo(
  mutation: Mutation,
  runId: Int,
  pos: Position,
  oldTree: String,
  mutated: String
)
