package org.scalamu.plugin

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.testutil.TestRunner

class MutationGuardSpec extends TestRunner with IsolatedScalamuCompilerFixture {
  override val mutations: Seq[Mutation] = ScalamuConfig.allMutations
  override val sanitizeTrees: Boolean   = true
  override val verifyTrees: Boolean     = true
  override val guard: MutationGuard     = FqnPrefixedGuard(ScalamuConfig.mutationGuardPrefix)

  "MutationGuard" should "not generate invalid ASTs" in withScalamuCompiler { (global, _) =>
    val guards =
      s"""
         |package ${ScalamuConfig.mutationGuardPrefix}
         |
         |object FooGuard {
         |  val enabledMutation = 1
         |}
    """.stripMargin
    val code =
      """
        |object Foo {
        |  val a = 123
        |  println(1 + a * -2)
        |}
      """.stripMargin
    compile(
      NamedSnippet("FooGuard.scala", guards),
      NamedSnippet("Foo.scala", code)
    )(global)
  }
}
