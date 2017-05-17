package org.scalamu.plugin.mutations

import org.scalamu.plugin.Mutation

/**
 * Defines a set of operators, to which mutation is applicable, and a set of rules,
 * which are then used to mutate them.
 */
private[mutations] trait OperatorMutationRules { self: Mutation =>
  protected def mutationRules: Map[String, String]
}
