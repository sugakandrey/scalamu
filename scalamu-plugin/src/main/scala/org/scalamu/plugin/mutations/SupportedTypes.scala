package org.scalamu.plugin.mutations

import org.scalamu.plugin.Mutation

import scala.tools.nsc.Global

/**
 * Allows a mutation to check if this tree should be mutated (according to its type).
 */
private[mutations] trait SupportedTypes { self: Mutation =>
  protected def supportedTypes(implicit global: Global): Seq[global.Type]
  
  protected def isAppropriatelyTyped(global: Global)(tree: global.Tree): Boolean
}
