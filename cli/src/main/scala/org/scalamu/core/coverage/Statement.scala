package org.scalamu.core.coverage

import org.scalamu.common.position.Position

/**
 * A single source level statement instrumented by scoverage plugin.
 *
 * @param id scoverage statement id
 * @param line line in the source file
 * @param pos statement's offset in source
 */
final case class Statement(id: Int, line: Int, pos: Position)
