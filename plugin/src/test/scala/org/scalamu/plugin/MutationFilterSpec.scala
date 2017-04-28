package org.scalamu.plugin

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.testutil.MutationTestRunner

class MutationFilterSpec extends MutationTestRunner with IsolatedScalamuCompilerFixture {

  override val filter: MutationFilter = RegexBasedFilter(
    ".*scala.Predef.print.*".r,
    ".*foobar.*".r,
    ".*Foo.Bar".r
  )
  override def mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations

  "MutationFilter" should "ignore symbols according to their fullName using supplied regex" in
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

  it should "not ignore any symbols if AcceptAllFilter is used" in withMutations { mutations =>
    withPluginConfig { cfg =>
      withScalamuCompiler(mutations, cfg.copy(filter = AcceptAllFilter)) { (global, reporter) =>
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
  }
}
