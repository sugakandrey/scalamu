package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin._

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
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutation: Mutation = self

    // @TODO: This does not play nicely with chained function calls, partially applied functions or implicit parameters.
    override protected val transformer: Transformer = new Transformer {
      override def mutate: PartialFunction[Tree, Tree] = {
        case TreeWithType(
            tree @ MaybeTypedApply(qualifier, name, args),
            tpe
            ) if qualifier.tpe <~< tpe && !name.containsName(nme.CONSTRUCTOR) =>
          val mutant      = q"$qualifier".setPos(tree.pos)
          val mutatedBody = super.transform(qualifier)
          val mutatedArgs = args.map(super.transform)
          generateMutantReport(tree, mutant)
          guard(mutant, q"$mutatedBody.$name(..$mutatedArgs)".setPos(tree.pos))
      }
    }
  }
}
