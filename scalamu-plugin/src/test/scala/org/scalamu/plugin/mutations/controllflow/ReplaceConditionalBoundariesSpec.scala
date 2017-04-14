package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceConditionalBoundariesSpec extends SingleMutationSpec {
  override def mutation: Mutation = ReplaceConditionalBoundaries

  "ReplaceConditionalBoundaries" should "change conditional boundaries" in withScalamuCompiler {
    (global, config) =>
      val code =
        """
          |object Foo {
          |  val a = 123
          |  def foo(x: Int): Boolean = a > 123
          |  
          |  if (a <= 123) {
          |    1 + 2
          |  }
          |  class Bar(val x: Int = if (a >= 0) 10 else 11)
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 3
  }

  it should "not change conditional boundaries for unsupported types" in withScalamuCompiler {
    (global, config) =>
      val code =
        """
          |object Foo {
          |  case class A(i: Int) {
          |    def <(other: A): Boolean = false
          |  }
          |  
          |  if (A(1) < A(2)) {
          |    println(1)
          |  }
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo shouldBe empty
  }
}
