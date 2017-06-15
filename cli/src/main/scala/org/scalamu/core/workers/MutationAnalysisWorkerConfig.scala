package org.scalamu.core.workers

import org.scalamu.core.configuration.Derivable

final case class MutationAnalysisWorkerConfig(
  timeoutFactor: Double,
  timeoutConst: Long,
  verbose: Boolean = false
)

object MutationAnalysisWorkerConfig {
  implicit val runnerConfigDerivable: Derivable[MutationAnalysisWorkerConfig] = config =>
    MutationAnalysisWorkerConfig(
      config.timeoutFactor,
      config.timeoutConst,
      config.verbose
  )
}
