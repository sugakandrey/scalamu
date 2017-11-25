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

    override protected val transformer: Transformer = new Transformer {
      override def mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"${Select(qualifier, name)}[..$targs](...$args)"
            if args.nonEmpty &&
              qualifier.tpe <~< tree.tpe &&
              !name.containsName(nme.CONSTRUCTOR) =>
          qualifier.safeDuplicate
      }
    }
  }
}
