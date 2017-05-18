package org.scalamu.plugin.mutations

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Base trait for mutations, which are applicable to `apply` method call trees.
 */
trait GenericApplyMutation extends Mutation { self: SupportedTypes =>
  protected def replaceWith(global: Global): global.Tree

  override def mutatingTransformer(
    global: Global,
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutation: Mutation = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case expr @ q"${TreeWithType(tree, tpe)}.apply[$targs](..$args)"
            if supportedTypes.exists(_ <:< tpe) =>
          
          val mutant = q"${replaceWith(global)}[$targs]".setPos(expr.pos)
          
          val mutatedArgs = args.map(super.transform)
          val alternative = q"$tree.apply[$targs](..$mutatedArgs)".setPos(expr.pos)
          generateMutantReport(expr, mutant)
          guard(mutant, alternative)
      }
    }
  }
}
