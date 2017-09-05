package org.scalamu.plugin.mutators

import org.scalamu.plugin.Mutator

import scala.tools.nsc.Global

/**
 * Allows a mutation operator to check if this tree should be mutated (according to its type).
 */
private[mutators] trait SupportedTypes { self: Mutator =>
  protected def supportedTypes(implicit global: Global): Seq[global.Type]
}
