package org.scalamu.plugin.mutators

import scala.tools.nsc.Global

case object ReplaceWithNone extends GenericApplyMutator with SupportedTypes {
  override def description: String = "Replaced Option.apply[T] with None"

  override protected def replaceWith(global: Global): global.Tree = 
    global.reify(Option.empty).tree

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] =
      Seq(global.definitions.OptionModule.tpe)
}
