package org.scalamu.plugin.mutators

import org.scalamu.plugin.{MutatingTransformer, ScalamuScalacConfig}

import scala.tools.nsc.Global

case object ReplaceWithNil extends AbstractApplyMutator {
  override def description: String = "Replaced List.apply[T] with Nil"

  override def mutatingTransformer(global: Global, config: ScalamuScalacConfig): MutatingTransformer =
    new ApplyTransformer(config)(global) {
      override protected def replaceWith(input: global.Tree): global.Tree =
        global.reify(List.empty).tree

      override protected def isApplicableType(tpe: global.Type): Boolean =
        tpe =:= global.definitions.ListModule.tpe
    }
}
