package org.scalamu.plugin.mutators

import scala.tools.nsc.Global

case object ReplaceWithNone extends GenericApplyMutator with SupportedTypes {
  override def description: String = "Replaced Option.apply[T] with None"

  override protected def replaceWith(global: Global): global.Tree = {
    import global._
    reify(Option.empty).tree
  }

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] = {
    import global.definitions.{OptionModule, SomeModule}
    Seq(
      SomeModule.tpe,
      OptionModule.tpe
    )
  }
}
