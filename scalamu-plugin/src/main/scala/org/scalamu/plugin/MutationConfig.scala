package org.scalamu.plugin

/**
 * Stores plugin configuration info.
 *
 * @param reporter      the [[org.scalamu.plugin.MutationReporter]],
 *                      used to save information about inserted mutants
 * @param guard         the [[org.scalamu.plugin.MutationGuard]], which is used
 *                      to "guard" inserted mutants (i.e. to allow to enable/disable them programmatically).
 * @param filter        the [[MutationFilter]], used to
 *                      exclude some symbols/files/classes from being mutated
 * @param verifyTrees   Should trees be checked for nested mutants
 * @param sanitizeTrees Should nested mutants be explicitly removed
 */
case class MutationConfig(
  reporter: MutationReporter,
  guard: MutationGuard,
  filter: MutationFilter,
  verifyTrees: Boolean,
  sanitizeTrees: Boolean
)
