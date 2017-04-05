package org.scalamu.plugin

case class MutationConfig(
  reporter: MutationReporter,
  guard: MutationGuard,
  verifyTrees: Boolean
)
