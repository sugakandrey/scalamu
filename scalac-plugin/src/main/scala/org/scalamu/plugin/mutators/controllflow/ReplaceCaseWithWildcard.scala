package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation operator that removes all but wildcard case expressions in pattern match.
 */
case object ReplaceCaseWithWildcard extends Mutator { self =>
  override def description: String = "Replaced pattern match with wildcard case"

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override protected def mutator: Mutator = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case tree @ q"$expr match { case ..${cases: List[Tree]} }" if cases.size > 1 =>
          val wildcardCase = cases.collectFirst {
            case c @ cq"${Ident(nme.WILDCARD)} => $body"         => c
            case c @ cq"$name @ ${Ident(nme.WILDCARD)} => $body" => c
          }
          wildcardCase.fold(tree) { wcc =>
            val mutant =
              q"${expr.duplicate} match { case ..${List(wcc.duplicate)} }".setPos(tree.pos)
            val id = generateMutantReport(tree, mutant)
            guard(mutant, tree, id)
          }
      }
    }
  }
}
