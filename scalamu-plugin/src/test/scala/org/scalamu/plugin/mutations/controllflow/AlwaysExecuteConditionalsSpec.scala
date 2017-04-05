package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.util.SingleMutationSpec

class AlwaysExecuteConditionalsSpec extends SingleMutationSpec {
  override def mutation: Mutation = AlwaysExecuteConditionals
  
  "AlwaysExecuteConditionals" should "replace conditional block with \'then\' branch" in withScalamuCompiler {
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
