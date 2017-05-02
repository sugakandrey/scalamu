package org.scalamu.common.position

/**
 * Position in a source file
 */
final case class Position(source: String, from: Int, to: Int) {
  def includes(other: Position): Boolean =
    source == other.source && (from <= other.from && to >= other.to)
}
