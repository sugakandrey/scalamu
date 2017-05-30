package org.scalamu.common.position

/**
 * Position in a source file
 */
final case class Position(source: String, line: Int, from: Int, to: Int) {
  def overlaps(other: Position): Boolean =
    source == other.source && (to >= other.from && other.to >= from)
}
