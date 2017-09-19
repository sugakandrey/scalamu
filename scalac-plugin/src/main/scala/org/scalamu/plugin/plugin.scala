package org.scalamu.plugin

import org.scalamu.plugin.util.CompilerAccess

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.transform.{Transform, TypingTransformers}

class ScalamuPlugin(
  val global: Global,
  val config: MutationConfig
) extends Plugin { plugin =>

  override val name: String                      = "scalamu"
  override val components: List[PluginComponent] = List(MutationComponent)
  override val description: String               = "scalamu mutation testing compiler plugin"

  case object MutationComponent
      extends PluginComponent
      with TypingTransformers
      with Transform
      with CompilerAccess
      with MutationVerifier {

    override val global: Global           = plugin.global
    override val phaseName: String        = "mutating-transform"
    override val runsAfter: List[String]  = List("typer")
    override val runsBefore: List[String] = List("patmat")

    import global._

    override def isMutationGuard(symbolName: String): Boolean =
      config.guard.isGuardSymbol(symbolName)

    override protected def newTransformer(unit: CompilationUnit): Transformer =
      new Transformer(unit)

    class Transformer(unit: CompilationUnit) extends TypingTransformer(unit) {
      override def transform(tree: Tree): Tree = {

        def applyTransformations(tree: Tree, mutations: List[MutatingTransformer]): Tree =
          mutations match {
            case Nil => tree
            case tr :: rest =>
              val intermediate = tr(tree.asInstanceOf[tr.global.Tree])
              applyTransformations(intermediate.asInstanceOf[Tree], rest)
          }
        val mutatedUnit = applyTransformations(
          tree,
          config.mutations.map(_.mutatingTransformer(global, config))(collection.breakOut)
        )
        if (config.verifyTrees) {
          val nestedMutations = treesWithNestedMutations(mutatedUnit)
          nestedMutations.foreach(
            t =>
              reporter
                .error(NoPosition, s"Tree ${show(t)} in unit $unit contains nested mutations.")
          )
        }
        mutatedUnit
      }
    }
  }
}
