package org.scalamu.core.process

import org.scalamu.common.MutationId
import org.scalamu.core.api.DetectionStatus

/**
 * Fully analysed mutant, with appropriate status.
 */
final case class MutationAnalysisProcessResponse(id: MutationId, status: DetectionStatus)
