package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceConditionalBoundariesSpec extends SingleMutationSpec {
  override def mutation: Mutator = ChangeConditionalBoundaries

  "ChangeConditionalBoundaries" should "change conditional boundaries" in withScalamuCompiler { (global, reporter) =>
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
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo should have size 3
  }

  it should "not change conditional boundaries for unsupported types" in withScalamuCompiler { (global, reporter) =>
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
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo shouldBe empty
  }
}
