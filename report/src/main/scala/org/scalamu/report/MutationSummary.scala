package org.scalamu.report

import org.scalamu.core.{SourceInfo, TestedMutant}
import org.scalamu.plugin.Mutator

final case class MutationSummary(
  source: SourceInfo,
  mutations: Set[Mutator],
  mutationResults: Seq[TestedMutant]
)
