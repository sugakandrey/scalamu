package org.scalamu.plugin.mutations

import org.scalamu.plugin.{FqnPrefixedMutationGuard, Mutation, MutationGuard, ScalamuConfig}
import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalamu.plugin.mutations.arithmetic.{InvertNegations, ReplaceMathOperators}
import org.scalamu.plugin.mutations.controllflow.{
  ReplaceCaseWithWildcard,
  ReplaceConditionalBoundaries
}
import org.scalamu.plugin.util.{CompilationUtils, MutationPhaseOnlyRunner}
import org.scalatest.{FlatSpec, Matchers}

import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}

class TreeSanitizerSpec
    extends FlatSpec
    with Matchers
    with MutationPhaseOnlyRunner
    with CompilationUtils
    with SharedScalamuCompilerFixture {
  // Purposefully wrong mutation order
  override def mutations: Seq[Mutation] = Seq(
    InvertNegations,
    ReplaceMathOperators,
    ReplaceConditionalBoundaries,
    ReplaceCaseWithWildcard
  )
  override lazy val guard: MutationGuard = FqnPrefixedMutationGuard(
    ScalamuConfig.mutationGuardPrefix
  )
  override lazy val reporter: Reporter = new ConsoleReporter(settings)
  override lazy val verifyTrees        = true
  override lazy val sanitizeTrees      = true

  "TreeSanitizer" should "remove nested mutants" in withScalamuCompiler { implicit global =>
    val guards =
      """
        |package org.scalamu.guards
        |object FooGuard {
        |  val enabledMutation = 1
        |}
      """.stripMargin
    val code =
      """
        |object Foo {
        |  val a = 123 
        |  val b = a * -2
        |  
        |  if (a * 2 >= 124) {
        |    println("nested mutation")
        |  }
        |  
        |  a match {
        |    case 123 => 0
        |    case _   => a * a
        |  }
        |}
      """.stripMargin
    compile(
      NamedSnippet("Guards.scala", guards),
      NamedSnippet("Foo.scala", code)
    )
  }
}
