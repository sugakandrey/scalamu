package org.scalamu.core.workers

import org.scalamu.common.MutantId
import org.scalamu.core.DetectionStatus

/**
 * Fully analysed mutant, with appropriate status.
 */
final case class MutationWorkerResponse(id: MutantId, status: DetectionStatus)
