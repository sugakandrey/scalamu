package org.scalamu.plugin

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.mutations.arithmetic.{InvertNegations, ReplaceMathOperators}
import org.scalamu.plugin.mutations.controllflow.{
  ReplaceCaseWithWildcard,
  ReplaceConditionalBoundaries
}
import org.scalamu.plugin.testutil.TestRunner

import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}

class MutationVerifierSpec extends TestRunner with IsolatedScalamuCompilerFixture {

  // Purposefully wrong mutation order
  override val mutations: Seq[Mutation] = Seq(
    InvertNegations,
    ReplaceMathOperators,
    ReplaceConditionalBoundaries,
    ReplaceCaseWithWildcard
  )

  override def createReporter(settings: Settings): Reporter = new ConsoleReporter(settings) {
    override def printMessage(msg: String): Unit = ()
  }
  override val guard: MutationGuard = FqnPrefixedGuard(ScalamuConfig.mutationGuardPrefix)
  override val verifyTrees          = true
  private val guards =
    s"""
       |package ${ScalamuConfig.mutationGuardPrefix}
       |object FooGuard {
       |  val enabledMutation = 1
       |}
       """.stripMargin

  "Mutation verifier" should "report compiler error if nested mutants were generated" in withScalamuCompiler {
    (global, _) =>
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
      val reporter = global.reporter
      reporter.count(reporter.ERROR) should ===(3)
      reporter.resetCount(reporter.ERROR)
  }

  private val noNestedOrder = Seq(
    ReplaceConditionalBoundaries,
    ReplaceCaseWithWildcard,
    ReplaceMathOperators,
    InvertNegations
  )

  it should "not reporter any errors if no nested mutants were generated" in withPluginConfig {
    config =>
      withScalamuCompiler(noNestedOrder, config) { (global, _) =>
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

}
