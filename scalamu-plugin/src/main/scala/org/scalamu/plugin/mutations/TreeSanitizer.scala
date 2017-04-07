package org.scalamu.plugin.mutations

trait TreeSanitizer { self: CompilerAccess with GlobalExtractors =>

  case object RemovingTransformer extends global.Transformer {
    import global._

    override def transform(tree: Tree): Tree = tree match {
      case GuardedMutation(_, _, untouched) => untouched
      case _                                => super.transform(tree)
    }
  }

  protected final def removeNestedMutants(tree: global.Tree): global.Tree =
    RemovingTransformer.transform(tree)
}
