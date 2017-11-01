package org.scalamu.core.process

import org.scalamu.core.configuration.Derivable

final case class MutationAnalysisProcessConfig(
  timeoutFactor: Double,
  timeoutConst: Long,
  verbose: Boolean = false
)

object MutationAnalysisProcessConfig {
  implicit val runnerConfigDerivable: Derivable[MutationAnalysisProcessConfig] = config =>
    MutationAnalysisProcessConfig(
      config.timeoutFactor,
      config.timeoutConst,
      config.verbose
  )
}
