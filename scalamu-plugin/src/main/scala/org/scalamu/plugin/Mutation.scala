package org.scalamu.plugin

import org.scalamu.plugin.mutations.TypeEnrichment

trait Mutation {
  def mutatingTransformer(ctx: MutationContext): MutatingTransformer
}

abstract class MutatingTransformer(val context: MutationContext) extends TypeEnrichment(context.global) {
  trait Transformer extends global.Transformer {
    import global._
    import typer.typed

    override final def transform(tree: Tree): Tree = tree match {
      case DefDef(mods, _, _, _, _, _) if mods.isSynthetic => tree
      case ClassDef(_, _, _, Template(parents, _, _))
          if parents.map(_.tpe.typeSymbol.fullName).contains("scala.reflect.api.TypeCreator") =>
        tree
      case _ =>
        val mutated = mutate.applyOrElse(tree, PartialFunction(continue))
        if (mutated != tree) {
          typed(mutated)
        } else mutated
    }

    protected final def reportMutation(tree: Tree, mutated: Tree): Unit = {
      val info = MutationInfo(
        mutation,
        global.currentRunId,
        tree.pos,
        show(tree),
        show(mutated)
      )
      context.mutationReporter.report(info)
    }

    protected final def continue: Tree => Tree = super.transform

    protected def mutate: PartialFunction[Tree, Tree]
  }

  protected final def mutationGuard(mutated: global.Tree, untouched: global.Tree): global.Tree = {
    import global._
    q"if (true) $mutated else $untouched"
  }

  protected def mutation: Mutation

  protected def transformer: Transformer

  def apply(tree: global.Tree): global.Tree = transformer.transform(tree)
}
