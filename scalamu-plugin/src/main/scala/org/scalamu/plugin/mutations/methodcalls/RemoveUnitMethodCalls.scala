package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationReporter}

import scala.tools.nsc.Global

/**
 * Mutation, that removes calls to methods with [[Unit]] return type.
 * e.g.
 * {{{
 * val a = 123
 * println(a)
 * }}}
 * is replaced with
 * {{{
 * val a = 123
 * ()
 * }}}
 */
case object RemoveUnitMethodCalls extends Mutation { self =>
  override def mutatingTransformer(
    global: Global,
    mutationReporter: MutationReporter
  ): MutatingTransformer = new MutatingTransformer(global, mutationReporter) {
    import global._

    override def mutation: Mutation = self

    override val transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case TreeWithType(
            tree @ Apply(fn, args),
            definitions.UnitTpe
            ) =>
          val mutatedArgs    = args.map(super.transform)
          val mutationResult = q"()"
          reportMutation(tree, mutationResult)
          mutationGuard(mutationResult, q"$fn(..$mutatedArgs)")
        case tree @ Function(
              List(params),
              TreeWithType(body, definitions.UnitTpe)
            ) =>
          val mutatedBody    = super.transform(body)
          val mutationResult = q"(..$params) => ()"
          reportMutation(tree, mutationResult)
          mutationGuard(mutationResult, q"(..$params) => $mutatedBody")
      }
    }
  }
}
