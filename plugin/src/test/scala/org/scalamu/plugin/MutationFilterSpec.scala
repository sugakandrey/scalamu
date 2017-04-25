package org.scalamu.plugin

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.testutil.TestRunner

class MutationFilterSpec extends TestRunner with IsolatedScalamuCompilerFixture {

  override val filter: MutationFilter = RegexBasedFilter(
    ".*scala.Predef.print.*".r,
    ".*foobar.*".r,
    ".*Foo.Bar".r
  )
  override def mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations

  "MutationFilter" should "ignore symbols according to their fullName using supplied regex" in
    withScalamuCompiler { (global, config) =>
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
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo shouldBe empty
    }

  it should "not ignore any symbols if AcceptAllFilter is used" in withMutations { mutations =>
    withPluginConfig { cfg =>
      withScalamuCompiler(mutations, cfg.copy(filter = AcceptAllFilter)) { (global, config) =>
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
        val mutantsInfo = mutantsFor(code)(global, config.reporter)
        mutantsInfo should have size 7
      }
    }
  }
}
