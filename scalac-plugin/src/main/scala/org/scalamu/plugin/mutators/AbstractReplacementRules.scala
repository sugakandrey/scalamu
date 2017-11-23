package org.scalamu.plugin.mutators

import org.scalamu.plugin.MutatingTransformer

private[mutators] trait AbstractReplacementRules extends DomainAware { 
  self: MutatingTransformer =>
  
  protected def replaceWith(input: Domain): global.Tree
}
