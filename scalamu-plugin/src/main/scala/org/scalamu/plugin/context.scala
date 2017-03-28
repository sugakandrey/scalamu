package org.scalamu.plugin

import scala.reflect.internal.util.Position
import scala.tools.nsc.Global

trait MutationReporter {
  def report(mutationInfo: MutationInfo): Unit
}

final case class MutationContext(global: Global, mutationReporter: MutationReporter)

final case class MutationInfo(
  mutation: Mutation,
  runId: Int,
  pos: Position,
  oldTree: String,
  mutated: String
)
