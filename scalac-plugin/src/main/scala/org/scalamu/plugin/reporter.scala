package org.scalamu.plugin

import org.scalamu.common.MutantId
import org.scalamu.common.position.Position

import scala.reflect.internal.util.{DefinedPosition, UndefinedPosition, Position => ReflectPosition}

/**
 * Used to save information about inserted mutants.
 */
trait MutationReporter {
  def report(mutantInfo: MutantInfo): Unit
  def mutants: Set[MutantInfo]
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
final case class MutantInfo(
  id: MutantId,
  mutation: Mutator,
  runId: Int,
  packageName: String,
  pos: Position,
  oldTree: String,
  mutated: String
) {
  def description: String = mutation.description
}

object MutantInfo {
  private var currentId = 0

  def apply(
    mutation: Mutator,
    runId: Int,
    packageName: String,
    pos: ReflectPosition,
    oldTree: String,
    mutated: String
  ): MutantInfo = {
    val position = pos match {
      case _: UndefinedPosition => Position(pos.source.path, 0, 0, 0)
      case _: DefinedPosition   => Position(pos.source.path, pos.line, pos.start, pos.end)
    }
    currentId += 1
    MutantInfo(
      MutantId(currentId),
      mutation,
      runId,
      packageName,
      position,
      oldTree,
      mutated
    )
  }
}
