package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationGuard, MutationReporter}

import scala.tools.nsc.Global

/**
 * Mutation, that removes all but wildcard case expressions in pattern match.
 */
case object ReplaceCaseWithWildcard extends Mutation { self =>
  override def mutatingTransformer(
    global: Global,
    mutationReporter: MutationReporter,
    mutationGuard: MutationGuard
  ): MutatingTransformer = new MutatingTransformer(mutationReporter, mutationGuard)(global) {
    import global._

    override protected def mutation: Mutation = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"$expr match { case ..${cases: List[Tree]} }" if cases.size > 1 =>
          val wildcard = cases.collectFirst {
            case c @ cq"${Ident(nme.WILDCARD)} => $body"         => c
            case c @ cq"$name @ ${Ident(nme.WILDCARD)} => $body" => c
          }
          wildcard.fold(tree) { wc =>
            val mutationResult = q"$expr match { case ..${List(wc)} }"
            reportMutation(tree, mutationResult)
            guard(mutationResult, tree)
          }
      }
    }
  }
}
