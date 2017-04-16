package org.scalamu.plugin.mutations

import scala.tools.nsc.Global

case object ReplaceWithNone extends GenericApplyMutation with SupportedTypes {
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
