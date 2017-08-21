package org.scalamu.core.compilation

import org.scalamu.plugin._
import org.scalamu.plugin.testutil.MutationTestRunner
import org.scalamu.testutil.{ScalamuSpec, TestingInstrumentationReporter}

class IgnoreCoverageStatementsFilterSpec
    extends ScalamuSpec
    with MutationTestRunner
    with IsolatedScalamuGlobalFixture {

  override def instrumentationReporter: TestingInstrumentationReporter =
    new TestingInstrumentationReporter
  override def mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations

  override val guard = FqnGuard(
    s"${ScalamuPluginConfig.mutationGuardPrefix}.FooGuard.enabledMutation"
  )

  private val guards =
    s"""
       |package ${ScalamuPluginConfig.mutationGuardPrefix}
       |object FooGuard {
       |  def enabledMutation: Int = 1
       |}
       """.stripMargin

  "IgnoreScoverageStatementsFilter" should "mutate scoverage instrumentation if disabled" in
    withScalamuGlobal { (global, reporter, instrumentation) =>
      val code =
        """
          |object Foo {
          |  println(123)
          |  val a = "Hello World!"
          |  val b = 10
          |  val c = b * b
          |}
          """.stripMargin
      compile(NamedSnippet("Guards.scala", guards))(global)
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 8
      instrumentation.statements() should have size 6
    }

  it should "ignore scoverage instrumentation if enabled" in withPluginConfig { cfg =>
    withScalamuGlobal(cfg.copy(filter = IgnoreCoverageStatementsFilter)) {
      (global, reporter, instrumentation) =>
        val code =
          """
            |object Foo {
            |  println(123)
            |  val a = "Hello World!"
            |  
            |  val b = 10
            |  val c = b * b
            |}
            """.stripMargin
        compile(NamedSnippet("Guards.scala", guards))(global)
        val mutantsInfo = mutantsFor(code)(global, reporter)
        mutantsInfo should have size 3
        instrumentation.statements() should have size 6
    }
  }
}
