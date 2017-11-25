package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin._
import org.scalamu.plugin.mutators.DomainAware

import scala.tools.nsc.Global

/**
 * Mutation operator that removes all but wildcard case expressions in pattern match.
 */
case object ReplaceCaseWithWildcard extends Mutator { self =>
  override def description: String = "Replaced pattern match with wildcard case"

  override def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) with DomainAware {
    import global._

    override protected type Domain = Match

    private def isWildCardCase(`case`: CaseDef): Boolean = `case` match {
      case cq"${Ident(nme.WILDCARD)} => $body"         => true
      case cq"$name @ ${Ident(nme.WILDCARD)} => $body" => true
      case _                                           => false
    }

    override protected def isApplicableTo(input: Match): Boolean =
      input.cases.size > 1 && input.cases.exists(isWildCardCase)

    override protected def mutator: Mutator = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case tree @ Match(selector, cases) if isApplicableTo(tree) =>
          val wcc    = cases.find(isWildCardCase).get
          val pos    = tree.pos.makeTransparent
          q"${selector.safeDuplicate} match { case ..${List(wcc.safeDuplicate)} }".setPos(pos)
      }
    }
  }
}
