package org.scalamu.plugin.util

/**
 * If [[org.scalamu.plugin.MutationConfig.sanitizeTrees]] is enabled,
 * `removeNestedMutants` is invoked on every mutated tree, to remove any
 * nested mutants.
 */
trait TreeSanitizer { self: CompilerAccess with GlobalExtractors =>

  protected case object SanitizingTransformer extends global.Transformer {
    import global._

    override def transform(tree: Tree): Tree = tree match {
      case GuardedMutant(_, _, untouched) => untouched
      case _                              => super.transform(tree)
    }

    def apply(tree: Tree): Tree = transform(tree)
  }
}
