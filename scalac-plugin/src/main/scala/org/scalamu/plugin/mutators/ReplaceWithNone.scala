package org.scalamu.plugin.mutators

import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

case object ReplaceWithNone extends AbstractApplyMutator {
  override def description: String = "Replaced Option.apply[T] with None"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new ApplyTransformer(config)(global) {
      override protected def replaceWith(input: global.Tree): global.Tree =
        global.reify(Option.empty).tree

      override protected def isApplicableType(tpe: global.Type): Boolean =
        tpe =:= global.definitions.OptionModule.tpe
    }
}
