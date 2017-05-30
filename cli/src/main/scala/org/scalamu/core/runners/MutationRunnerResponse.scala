package org.scalamu.core.runners

import org.scalamu.common.MutantId
import org.scalamu.core.DetectionStatus

/**
 * Fully analysed mutant, with appropriate status.
 */
final case class MutationRunnerResponse(id: MutantId, status: DetectionStatus)
