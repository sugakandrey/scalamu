package org.scalamu.plugin.mutations.conditionals

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalamu.plugin.{Mutation, MutationOnlyRunner}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class NeverExecuteConditionalsSpec
    extends FlatSpec
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture {

  override def mutations: Seq[Mutation] = List(NeverExecuteConditionals)

  "NeverExecuteConditionals" should "replace all conditional block with \'else\' branch" in withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          |  val bool = false
          |  val a = 123
          |  val b = if (a < 100) {
          |    a + 1
          |  } else a - 1
          |  
          |  if (b.isWhole()) {
          |    if (a == b) {}
          |    else println(123)
          |  }
          |  
          |  class Bar(val x: Int = if (bool) 10 else 11)
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 4
  }
}
