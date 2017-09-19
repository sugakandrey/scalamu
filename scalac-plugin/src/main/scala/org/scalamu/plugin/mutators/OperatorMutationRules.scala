package org.scalamu.plugin.mutators

import org.scalamu.plugin.Mutator

/**
 * Defines a set of operators, to which mutator is applicable, and a set of rules,
 * which are then used to mutate them.
 */
private[mutators] trait OperatorMutationRules { self: Mutator =>
  protected def mutationRules: Map[String, String]
}
