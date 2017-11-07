package org.scalamu.plugin

import org.scalamu.common.MutationId
import org.scalamu.common.position.Position

import scala.reflect.internal.util.{DefinedPosition, UndefinedPosition, Position => ReflectPosition}

/**
 * Used to save information about inserted mutants.
 */
trait MutationReporter {
  def report(mutantInfo: MutationInfo): Unit
  def mutations: Set[MutationInfo]
}

/**
 * Contains information about a single inserted mutant.
 *
 * @param mutation [[org.scalamu.plugin.Mutator]], that spawned this mutant
 * @param runId    Current global run id
 * @param pos      Original tree position
 * @param oldTree  Original tree
 * @param mutated  Mutated tree
 */
final case class MutationInfo(
                               id: MutationId,
                               mutation: Mutator,
                               runId: Int,
                               packageName: String,
                               pos: Position,
                               oldTree: String,
                               mutated: String
) {
  def description: String = mutation.description
}

object MutationInfo {
  private[this] var currentId = 0

  def apply(
    mutation: Mutator,
    runId: Int,
    packageName: String,
    pos: ReflectPosition,
    oldTree: String,
    mutated: String
  ): MutationInfo = {
    val position = pos match {
      case _: UndefinedPosition => Position(pos.source.path, 0, 0, 0)
      case _: DefinedPosition   => Position(pos.source.path, pos.line, pos.start, pos.end)
    }
    
    currentId += 1

    MutationInfo(
      MutationId(currentId),
      mutation,
      runId,
      packageName,
      position,
      oldTree,
      mutated
    )
  }
}
