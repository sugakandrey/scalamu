package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.{Mutation, SingleMutationSpec}

class ReplaceLogicalOperatorsSpec extends SingleMutationSpec {
  
  override def mutations: Seq[Mutation] = List(ReplaceLogicalOperators)
  
  "ReplaceLogicalOperators" should "swap \'&&\' and \'||\' operators" in withScalamuCompiler {
    implicit global =>
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
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 6
  }
  
  it should "not support types other than boolean" in withScalamuCompiler {
    implicit global =>
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
      val mutationsInfo = mutationsFor(code)
      mutationsInfo shouldBe empty
  }
}
