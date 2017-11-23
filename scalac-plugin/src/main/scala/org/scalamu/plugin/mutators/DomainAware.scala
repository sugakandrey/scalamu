package org.scalamu.plugin.mutators

import org.scalamu.plugin.MutatingTransformer

private[mutators] trait DomainAware { self: MutatingTransformer =>
  protected type Domain <: global.Tree
  
  protected def isApplicableTo(input: Domain): Boolean = true
}
