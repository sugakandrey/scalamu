package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationGuard, MutationReporter}

import scala.tools.nsc.Global

/**
 * Mutation, that replaces appropriately typed method calls and function literals with identity.
 * (More formally: only function literals of type forall a. a => a and method calls, such that
 * lhs.method(..args) typeOf[lhs] ~ typeOf[lhs.method(..args)] are mutated).
 * e.g.
 * {{{
 * List(1, 2, 3).map(_ + 1)
 * Set(1, 2) -- Set(1)
 * }}}
 * is mutated to
 * {{{
 * List(1, 2, 3).map(x => x)
 * Set(1, 2)
 * }}}
 *
 */
case object ReplaceWithIdentityFunction extends Mutation { self =>
  override def mutatingTransformer(
    global: Global,
    mutationReporter: MutationReporter,
    mutationGuard: MutationGuard
  ): MutatingTransformer = new MutatingTransformer(mutationReporter, mutationGuard)(global) {
    import global._

    override protected def mutation: Mutation = self

    override protected val transformer: Transformer = new Transformer {
      override def mutate: PartialFunction[Tree, Tree] = {
        case tree @ Function(
              arg :: Nil,
              TreeWithType(exprs, tpe)
            ) if tpe ~ arg.tpt.tpe =>
          val mutationResult = q"(..${List(arg)}) => ${arg.symbol}"
          val mutatedExprs   = super.transform(exprs)
          reportMutation(tree, mutationResult)
          guard(mutationResult, q"(..${List(arg)}) => $mutatedExprs")
        case TreeWithType(
            tree @ Apply(Select(body, method @ TermName(name)), args),
            tpe
            ) if tpe ~ body.tpe && !name.contains("<init>") =>
          val mutationResult = q"$body"
          val mutatedBody    = super.transform(body)
          val mutatedArgs    = args.map(super.transform)
          reportMutation(tree, mutationResult)
          guard(mutationResult, q"$mutatedBody.$method(..$mutatedArgs)")
      }
    }
  }
}
