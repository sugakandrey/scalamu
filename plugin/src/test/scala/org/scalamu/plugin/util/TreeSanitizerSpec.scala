package org.scalamu.plugin.util

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.mutations.arithmetic.{InvertNegations, ReplaceMathOperators}
import org.scalamu.plugin.mutations.controllflow.{
  ReplaceCaseWithWildcard,
  ReplaceConditionalBoundaries
}
import org.scalamu.plugin.testutil.MutationTestRunner
import org.scalamu.plugin.{FqnPrefixedGuard, Mutation, MutationGuard, ScalamuPluginConfig}
import org.scalatest.{FlatSpec, Matchers}

class TreeSanitizerSpec
    extends FlatSpec
    with Matchers
    with MutationTestRunner
    with IsolatedScalamuCompilerFixture {

  // Purposefully wrong mutation order
  override def mutations: Seq[Mutation] = Seq(
    InvertNegations,
    ReplaceMathOperators,
    ReplaceConditionalBoundaries,
    ReplaceCaseWithWildcard
  )
  override val guard: MutationGuard = FqnPrefixedGuard(ScalamuPluginConfig.mutationGuardPrefix)
  override val verifyTrees          = true
  override val sanitizeTrees        = true

  "TreeSanitizer" should "remove nested mutants" in withScalamuCompiler { (global, _) =>
    val guards =
      s"""
         |package ${ScalamuPluginConfig.mutationGuardPrefix}
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
        |    println("nested mutant")
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
    )(global)
  }
}
