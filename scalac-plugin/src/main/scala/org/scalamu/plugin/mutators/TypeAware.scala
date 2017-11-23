package org.scalamu.plugin.mutators

import org.scalamu.plugin.MutatingTransformer

/**
 * Allows a mutation operator to check if this tree should be mutated (according to its type).
 */
private[mutators] trait TypeAware { self: MutatingTransformer =>
  protected def isApplicableType(tpe: global.Type): Boolean
}
