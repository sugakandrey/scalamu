package org.scalamu.plugin.mutators.methodcalls

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation operator that removes calls to methods with [[Unit]] return type.
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
case object RemoveUnitMethodCalls extends Mutator { self =>
  override def description: String = "Removed method call with Unit return type"

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override def mutator: Mutator = self

    override val transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case TreeWithType(
            tree: Apply,
            definitions.UnitTpe
            ) =>
          q"()".setPos(tree.pos.makeTransparent)
      }
    }
  }
}
