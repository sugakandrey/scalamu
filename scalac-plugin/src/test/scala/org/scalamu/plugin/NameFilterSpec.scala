package org.scalamu.plugin

import org.scalamu.common.filtering.{AcceptAllFilter, InverseRegexFilter, NameFilter, RegexFilter}
import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.mutators.arithmetic.ReplaceMathOperators
import org.scalamu.plugin.mutators.methodcalls.RemoveUnitMethodCalls
import org.scalamu.plugin.testutil.MutationTestRunner

class NameFilterSpec extends MutationTestRunner with IsolatedScalamuCompilerFixture {

  override val filter: NameFilter = InverseRegexFilter(
    "scala\\.Predef\\.print.*".r,
    ".*foobar.*".r,
    ".*Foo.Bar".r
  )
  override def mutators: Seq[Mutator] = ScalamuPluginConfig.allMutators

  "NameFilter" should "ignore symbols according to their fullName using supplied regex" in
    withScalamuCompiler { (global, reporter) =>
      val code =
        """
          |object Foo {
          |  println(123)
          |  print("Hello World!")
          |
          |  def foobar(a: Int): Int = a - 10
          |
          |  foobar(-100)
          |
          |  class Bar(i: Int, s: String) {
          |    println(123)
          |    val a = -100
          |  }
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo shouldBe empty
    }

  it should "not ignore any symbols if AcceptAllFilter is used" in withPluginConfig { cfg =>
    withScalamuCompiler(mutators, cfg.copy(ignoreSymbols = AcceptAllFilter)) { (global, reporter) =>
      val code =
        """
          |object Foo {
          |  println(123)
          |  print("Hello World!")
          |
          |  def foobar(a: Int): Int = a - 10
          |
          |  foobar(-100)
          |
          |  class Bar(i: Int, s: String) {
          |    println(1)
          |    val a = -100
          |  }
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 7
    }
  }

  it should "ignore other trees if targetOwners filter is set" in withPluginConfig { cfg =>
    withScalamuCompiler(
      Seq(ReplaceMathOperators, RemoveUnitMethodCalls),
      cfg.copy(targetOwners = RegexFilter("""foo\.example\.Foo.*""".r))
    ) { (global, report) =>
      val code =
        """
          |package foo.example
          |
          |object Foo {
          |  println(123)
          |  val x = 42
          |  def foo(): Int = x * x
          |}
          |
          |object Bar {
          |  println(456)
          |  val y = 21
          |  def bar(): Int = y + y
          |}
          """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, report)
      mutantsInfo should have size 1
    }
  }
}
