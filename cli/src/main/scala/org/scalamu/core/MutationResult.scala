package org.scalamu.core

import org.scalamu.plugin.MutantInfo

/**
 * Fully analysed mutant, with appropriate status.
 */
final case class MutationResult(info: MutantInfo, status: DetectionStatus)
