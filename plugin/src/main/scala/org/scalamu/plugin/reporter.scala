package org.scalamu.plugin

import org.scalamu.common.position.Position

import scala.reflect.internal.util.{Position => ReflectPosition}

/**
 * Used to save information about inserted mutants.
 */
trait MutationReporter {
  def report(mutantInfo: MutantInfo): Unit
}

/**
 * Contains information about a single inserted mutant.
 *
 * @param mutation [[org.scalamu.plugin.Mutation]], that spawned this mutant
 * @param runId Current global run id
 * @param pos Original tree position
 * @param oldTree Original tree
 * @param mutated Mutated tree
 */
final case class MutantInfo(
  mutation: Mutation,
  runId: Int,
  pos: Position,
  oldTree: String,
  mutated: String
)

object MutantInfo {
  def apply(
    mutation: Mutation,
    runId: Int,
    pos: ReflectPosition,
    oldTree: String,
    mutated: String
  ): MutantInfo = {
    val position = Position(pos.source.path, pos.start, pos.end)
    MutantInfo(mutation, runId, position, oldTree, mutated)
  }
}
