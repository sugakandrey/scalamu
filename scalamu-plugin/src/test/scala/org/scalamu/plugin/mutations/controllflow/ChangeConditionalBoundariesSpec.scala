package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.{Mutation, MutationOnlyRunner}
import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ChangeConditionalBoundariesSpec
    extends FlatSpec
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture {
  
  override def mutations: Seq[Mutation] = List(ChangeConditionalBoundaries)
  
  "ChangeConditionalBoundaries" should "change conditional boundaries" in withScalamuCompiler {
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
  
  it should "not change conditional boundaries for unsupported types" in withScalamuCompiler {
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
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo shouldBe empty
  }
}
