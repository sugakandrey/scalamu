package org.scalamu.plugin.mutators.literals

import org.scalamu.plugin.{MutatingTransformer, Mutator, ScalamuScalacConfig}
import org.scalamu.plugin.mutators.SupportedTypes

import scala.tools.nsc.Global

trait LiteralMutator extends Mutator with SupportedTypes { self =>
  protected def replaceWith(global: Global)(original: global.Constant): global.Constant

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutator: Mutator = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"${const: Constant}" if supportedTypes.exists(_ =:= const.tpe) =>
          val replacement = replaceWith(global)(const)
          val mutant      = treeCopy.Literal(tree, replacement)
          val id          = generateMutantReport(tree, mutant)
          guard(mutant, tree, id)
      }
    }
  }
}
