package org.scalamu.plugin.mutators

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Base trait for mutation operators, which are applicable to `apply` method call trees.
 */
trait GenericApplyMutator extends Mutator { self: SupportedTypes =>
  protected def replaceWith(global: Global): global.Tree

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutator: Mutator = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case expr @ q"${TreeWithType(tree, tpe)}.apply[$targs](..$args)" if supportedTypes.exists(_ =:= tpe) =>
          val mutant = q"${replaceWith(global)}[$targs]".setPos(expr.pos)

          val mutatedArgs = args.map(super.transform)
          val alternative = q"$tree.apply[$targs](..$mutatedArgs)".setPos(expr.pos)

          val id = generateMutantReport(expr, mutant)
          guard(mutant, alternative, id)
      }
    }
  }
}
