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

    override final def transform(tree: Tree): Tree =
      tree match {
        case DefDef(mods, _, _, _, _, _) if tree.symbol.isSynthetic => tree
        case ClassDef(_, _, _, Template(parents, _, _))
            if parents.map(_.tpe.typeSymbol.fullName).contains("scala.reflect.api.TypeCreator") =>
          tree
        case _ =>
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
  }

  protected def mutation: Mutation

  protected def transformer(): Transformer

  def apply(tree: global.Tree): global.Tree = transformer().transform(tree)
}
