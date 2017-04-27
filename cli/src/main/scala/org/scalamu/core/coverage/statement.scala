package org.scalamu.core.coverage

import com.typesafe.scalalogging.Logger
import org.scalamu.plugin.MutantInfo

/**
 * A single source level statement instrumented by scoverage plugin.
 *
 * @param source .scala file
 * @param line line in the source file
 * @param pos statement's offset in source
 */
final case class Statement(source: String, line: Int, pos: Position)

/**
 * Position in a source file
 */
final case class Position(from: Int, to: Int) {
  import Position._

  def includes(other: Position): Boolean = from <= other.from && to >= other.to
  def includes(info: MutantInfo): Boolean = {
    val pos = info.pos
    if (pos.isDefined)
      includes(Position(pos.start, pos.end))
    else {
      log.warn(s"Mutant $info position is undefined. Can't apply test coverage info.")
      false
    }
  }
}

object Position {
  private val log = Logger[Position]
}
