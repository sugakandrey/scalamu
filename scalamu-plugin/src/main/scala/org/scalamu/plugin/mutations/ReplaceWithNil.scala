package org.scalamu.plugin.mutations
import scala.tools.nsc.Global

case object ReplaceWithNil extends GenericApplyMutation with SupportedTypes {
  override protected def replaceWith(global: Global): global.Tree = {
    import global._
    reify(List.empty).tree
  }

  override protected def supportedTypes(implicit global: Global): Seq[global.Type] = {
    import global.definitions
    Seq(
      definitions.ListModule.tpe
    )
  }
}
