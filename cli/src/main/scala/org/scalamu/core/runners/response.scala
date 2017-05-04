package org.scalamu.core.runners

import io.circe.{KeyDecoder, KeyEncoder}
import org.scalamu.core.DetectionStatus

/**
 * Fully analysed mutant, with appropriate status.
 */
final case class MutationRunnerResponse(id: MutantId, status: DetectionStatus)

/**
 * Unique mutant identifier
 */
final case class MutantId(id: Int) extends AnyVal

object MutantId {
  implicit val keyEncoder: KeyEncoder[MutantId] = id =>
    KeyEncoder.encodeKeyInt(id.id)

  implicit val keyDecoder: KeyDecoder[MutantId] = key =>
    KeyDecoder.decodeKeyInt(key).map(MutantId(_))
}
