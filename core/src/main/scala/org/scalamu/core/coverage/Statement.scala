package org.scalamu.core.coverage

import org.scalamu.common.position.Position
import scoverage.Location

/**
 * A single source level statement instrumented by scoverage plugin.
 *
 * @param id scoverage statement id
 * @param pos statement's offset in source
 */
final case class Statement(id: StatementId, location: Location, pos: Position)
