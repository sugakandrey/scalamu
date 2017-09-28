package org.scalamu.plugin.mutators.methodcalls

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation operator that replaces appropriately typed method calls and function literals with identity.
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
case object ReplaceWithIdentityFunction extends Mutator { self =>
  override def description: String = "Replaced expression, typed A => A, with its lhs"

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutator: Mutator = self

    // @TODO: This does not play nicely with chained function calls, partially applied functions or implicit parameters.
    override protected val transformer: Transformer = new Transformer {
      override def mutate: PartialFunction[Tree, Tree] = {
        case TreeWithType(
            tree @ MaybeTypedApply(qualifier, name, args),
            tpe
            ) if qualifier.tpe <~< tpe && !name.containsName(nme.CONSTRUCTOR) =>
          val mutant      = qualifier.duplicate
          val mutatedBody = super.transform(qualifier)
          val mutatedArgs = args.map(super.transform)
          val id = generateMutantReport(tree, mutant)
          guard(mutant, q"$mutatedBody.$name(..$mutatedArgs)".setPos(tree.pos), id)
      }
    }
  }
}
