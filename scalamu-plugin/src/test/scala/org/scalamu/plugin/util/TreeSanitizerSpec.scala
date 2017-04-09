package org.scalamu.plugin.util

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalamu.plugin.mutations.arithmetic.{InvertNegations, ReplaceMathOperators}
import org.scalamu.plugin.mutations.controllflow.{
  ReplaceCaseWithWildcard,
  ReplaceConditionalBoundaries
}
import org.scalamu.plugin.testutil.{CompilationUtils, MutationPhaseOnlyRunner}
import org.scalamu.plugin.{FqnPrefixedGuard, Mutation, MutationGuard, ScalamuConfig}
import org.scalatest.{FlatSpec, Matchers}

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
  override lazy val guard: MutationGuard = FqnPrefixedGuard(
    ScalamuConfig.mutationGuardPrefix
  )
  override lazy val verifyTrees   = true
  override lazy val sanitizeTrees = true

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
