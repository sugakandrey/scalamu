package org.scalamu.plugin.util

/**
 * If [[org.scalamu.plugin.MutationConfig.sanitizeTrees]] is enabled,
 * `removeNestedMutants` is invoked on every mutated tree, to remove any
 * nested mutants.
 */
trait TreeSanitizer { self: CompilerAccess with GlobalExtractors =>

  private[this] case object RemovingTransformer extends global.Transformer {
    import global._

    override def transform(tree: Tree): Tree = tree match {
      case GuardedMutation(_, _, untouched) => untouched
      case _                                => super.transform(tree)
    }
  }

  protected final def removeNestedMutants(tree: global.Tree): global.Tree =
    RemovingTransformer.transform(tree)
}
