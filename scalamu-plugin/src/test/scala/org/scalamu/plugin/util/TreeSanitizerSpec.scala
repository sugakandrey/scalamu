package org.scalamu.plugin.util

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.mutations.arithmetic.{InvertNegations, ReplaceMathOperators}
import org.scalamu.plugin.mutations.controllflow.{
  ReplaceCaseWithWildcard,
  ReplaceConditionalBoundaries
}
import org.scalamu.plugin.testutil.TestRunner
import org.scalamu.plugin.{FqnPrefixedGuard, Mutation, MutationGuard, ScalamuConfig}
import org.scalatest.{FlatSpec, Matchers}

class TreeSanitizerSpec
    extends FlatSpec
    with Matchers
    with TestRunner
    with IsolatedScalamuCompilerFixture {

  // Purposefully wrong mutation order
  override def mutations: Seq[Mutation] = Seq(
    InvertNegations,
    ReplaceMathOperators,
    ReplaceConditionalBoundaries,
    ReplaceCaseWithWildcard
  )
  override val guard: MutationGuard = FqnPrefixedGuard(ScalamuConfig.mutationGuardPrefix)
  override val verifyTrees          = true
  override val sanitizeTrees        = true

  "TreeSanitizer" should "remove nested mutants" in withScalamuCompiler { (global, _) =>
    val guards =
      s"""
         |package ${ScalamuConfig.mutationGuardPrefix}
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
    )(global)
  }
}
