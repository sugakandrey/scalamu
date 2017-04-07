package org.scalamu.plugin

/**
 * Stores plugin configuration info.
 *
 * @param reporter [[org.scalamu.plugin.MutationReporter]] instance,
 *                used to save information about mutants
 * @param guard The mechanism used to allow enabling mutants programmatically
 * @param verifyTrees Should trees be checked for nested mutants
 * @param sanitizeTrees Should nested mutants be explicitly removed
 */
case class MutationConfig(
  reporter: MutationReporter,
  guard: MutationGuard,
  verifyTrees: Boolean,
  sanitizeTrees: Boolean
)
