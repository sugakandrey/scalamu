package org.scalamu.plugin

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.testutil.MutationTestRunner

class MutationGuardSpec extends MutationTestRunner with IsolatedScalamuCompilerFixture {
  override val mutators: Seq[Mutator] = ScalamuPluginConfig.allMutators
  override val sanitizeTrees: Boolean  = true
  override val verifyTrees: Boolean    = true

  override val guard: MutationGuard = FqnGuard(
    s"${ScalamuPluginConfig.mutationGuardPrefix}.FooGuard.enabledMutation"
  )

  "MutationGuard" should "not generate invalid ASTs" in withScalamuCompiler { (global, _) =>
    val guards =
      s"""
         |package ${ScalamuPluginConfig.mutationGuardPrefix}
         |
         |object FooGuard {
         |  def enabledMutation = 1
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
