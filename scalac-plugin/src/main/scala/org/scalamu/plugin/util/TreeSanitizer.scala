package org.scalamu.plugin.util

/**
 * If [[org.scalamu.plugin.ScalamuScalacConfig.sanitizeTrees]] is enabled,
 * `removeNestedMutants` is invoked on every mutated tree, to remove any
 * nested mutants.
 */
trait TreeSanitizer { self: CompilerAccess with GlobalExtractors =>
  import global._

  protected case object NestedMutantRemover extends Transformer {
    override def transform(tree: Tree): Tree = tree match {
      case GuardedMutant(_, _, untouched) => transform(untouched)
      case _                              => super.transform(tree)
    }

    def apply(tree: Tree): Tree = transform(tree)
  }

  protected case object TreePrettifier extends Transformer {
    override def transform(tree: Tree): Tree = tree match {
      case GuardedMutant(_, _, untouched)    => transform(untouched)
      case ScoverageInstrumentedStatement(t) => transform(t)
      case _                                 => super.transform(tree)
    }

    def apply(tree: Tree): Tree = transform(tree)
  }
}
