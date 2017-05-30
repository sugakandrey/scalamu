package org.scalamu.report

import org.scalamu.core.{SourceInfo, TestedMutant}
import org.scalamu.plugin.Mutation

final case class MutationSummary(
  source: SourceInfo,
  mutations: Set[Mutation],
  mutationResults: Seq[TestedMutant]
)
