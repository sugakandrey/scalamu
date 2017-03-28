package org.scalamu.plugin

/**
 * Created by sugakandrey.
 */
trait Mutation {
  def mutatingTransformer(ctx: MutationContext): MutatingTransformer
}

abstract class MutatingTransformer(val context: MutationContext) {
  import context.global

  trait Transformer extends global.Transformer {
    import global._
    import typer.typed

    override final def transform(tree: Tree): Tree = {
      val mutated = mutate(tree)
      if (mutated != tree) {
        val result = typed(mutated)
        context.mutationReporter.report(
          MutationInfo(
            mutation,
            global.currentRunId,
            tree.pos,
            show(tree),
            show(result)
          )
        )
        result
      } else mutated
    }

    protected def mutate(tree: Tree): Tree

    final def continue(tree: Tree): Tree = super.transform(tree)
  }

  protected def mutation: Mutation

  protected final def continue(tree: global.Tree): global.Tree = transformer.continue(tree)

  protected val transformer: Transformer

  def apply(tree: global.Tree): global.Tree = transformer.transform(tree)
}
