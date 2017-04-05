package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.util.SingleMutationSpec

class NegateConditionalsSpec extends SingleMutationSpec {
  override def mutation: Mutation = NegateConditionals

  "NegateConditionals" should "negate conditional operators" in withScalamuCompiler {
    implicit global =>
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
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 3
  }

  it should "not negate controllflow operators on unsupported types" in withScalamuCompiler {
    implicit global =>
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
          |  
          |  val eq = A(1) == A(2)
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo.shouldBe(empty)
  }
}
