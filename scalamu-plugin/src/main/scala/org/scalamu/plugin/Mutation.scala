package org.scalamu.plugin

import org.scalamu.plugin.mutations.{CompilerAccess, GlobalExtractors, TypeEnrichment}

import scala.tools.nsc.Global

trait Mutation {
  def mutatingTransformer(
    global: Global,
    mutationReporter: MutationReporter,
    mutationGuard: MutationGuard
  ): MutatingTransformer
}

abstract class MutatingTransformer(
  val mutationReporter: MutationReporter,
  val mutationGuard: MutationGuard
)(
  override implicit val global: Global
) extends CompilerAccess
    with GlobalExtractors
    with TypeEnrichment {

  trait Transformer extends global.Transformer {
    import global._

    override final def transform(tree: Tree): Tree = tree match {
      case DefDef(mods, _, _, _, _, _) if mods.isSynthetic => tree
      case ClassDef(_, _, _, Template(parents, _, _))
          if parents.map(_.tpe.typeSymbol.fullName).contains("scala.reflect.api.TypeCreator") =>
        tree
      case GuardedMutation(guard, mutated, alternative) =>
        // do not mutate code generated by another mutation
        q"if ($guard) $mutated else ${transform(alternative)}"
      case _ => mutate.applyOrElse(tree, continue)
    }

    protected final def reportMutation(tree: Tree, mutated: Tree): Unit = {
      val info = MutationInfo(
        mutation,
        currentRunId,
        tree.pos,
        show(tree),
        show(mutated)
      )
      mutationReporter.report(info)
    }

    protected final def continue: PartialFunction[Tree, Tree] = PartialFunction(super.transform)

    protected def mutate: PartialFunction[Tree, Tree]

    protected final def guard(mutated: Tree, untouched: Tree): Tree =
      mutationGuard(global)(mutated, untouched)
  }

  protected def mutation: Mutation

  protected def transformer: Transformer

  def apply(tree: global.Tree): global.Tree = transformer.transform(tree)
}
