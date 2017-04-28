package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceLogicalOperatorsSpec extends SingleMutationSpec {
  override def mutation: Mutation = ReplaceLogicalOperators

  "ReplaceLogicalOperators" should "swap \'&&\' and \'||\' operators" in withScalamuCompiler {
    (global, reporter) =>
      val code =
        """
          |class Foo {
          |  val a = true
          |  val b = a && false
          |  val c = a || a
          |  println(b || b && true)
          |  
          |  class Bar(b: Boolean = c && b || a)
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 6
  }

  it should "not support types other than boolean" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        |  case class Bar(b: Boolean) {
        |    def &&(other: Bar): Boolean = ???
        |  }
        |
        |  val a = Bar(false) && Bar(true)
        |}
        """.stripMargin
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo shouldBe empty
  }
}
