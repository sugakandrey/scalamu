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

  private val guards =
    s"""
       |package ${ScalamuPluginConfig.mutationGuardPrefix}
       |object FooGuard {
       |  val enabledMutation = 1
       |}
       """.stripMargin

  "IgnoreScoverageStatementsFilter" should "mutate scoverage instrumentation if disabled" in
    withScalamuGlobal { (global, reporter, instrumentation) =>
      val code =
        """
          |object Foo {
          |  println(123)
          |  val a = "Hello World!"
          |}
          """.stripMargin
      compile(NamedSnippet("Guards.scala", guards))(global)
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 3
      instrumentation.statements() should have size 3
    }

  it should "ignore scoverage instrumentation if enabled" in withPluginConfig { cfg =>
    withScalamuGlobal(cfg.copy(filter = IgnoreCoverageStatementsFilter(Seq()))) {
      (global, reporter, instrumentation) =>
        val code =
          """
            |object Foo {
            |  println(123)
            |  val a = "Hello World!"
            |}
            """.stripMargin
        compile(NamedSnippet("Guards.scala", guards))(global)
        val mutantsInfo = mutantsFor(code)(global, reporter)
        mutantsInfo should have size 1
        instrumentation.statements() should have size 3
    }
  }
}
