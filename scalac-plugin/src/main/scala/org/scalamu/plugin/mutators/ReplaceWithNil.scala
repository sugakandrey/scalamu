package org.scalamu.plugin.mutators

import scala.tools.nsc.Global

case object ReplaceWithNil extends GenericApplyMutator with SupportedTypes {
  override def description: String = "Replaced List.apply[T] with Nil"

  override protected def replaceWith(global: Global): global.Tree =
    global.reify(List.empty).tree

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
    Seq(global.definitions.ListModule.tpe)
}
