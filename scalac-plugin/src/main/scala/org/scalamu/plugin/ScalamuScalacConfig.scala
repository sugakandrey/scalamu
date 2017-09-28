package org.scalamu.plugin

import org.scalamu.common.filtering.{AcceptAllFilter, NameFilter}

/**
 * Stores plugin configuration info.
 *
 * @param reporter      the [[org.scalamu.plugin.MutationReporter]],
 *                      used to save information about inserted mutants
 * @param guard         the [[org.scalamu.plugin.MutationGuard]], which is used
 *                      to "guard" inserted mutants (i.e. to allow to enable/disable them programmatically).
 * @param ignoreSymbols the [[NameFilter]], used to
 *                      exclude some symbols from being mutated
 * @param verifyTrees   Should trees be checked for nested mutants
 * @param sanitizeTrees Should nested mutants be explicitly removed
 */
final case class ScalamuScalacConfig(
  reporter: MutationReporter,
  guard: MutationGuard,
  ignoreSymbols: NameFilter = AcceptAllFilter,
  mutators: Seq[Mutator] = ScalamuPluginConfig.allMutators,
  targetClasses: NameFilter = AcceptAllFilter,
  verifyTrees: Boolean = false,
  sanitizeTrees: Boolean = true
)
