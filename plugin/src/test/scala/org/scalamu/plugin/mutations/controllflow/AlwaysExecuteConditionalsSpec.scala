package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class AlwaysExecuteConditionalsSpec extends SingleMutationSpec {
  override def mutation: Mutation = AlwaysExecuteConditionals

  "AlwaysExecuteConditionals" should "replace conditional block with \'then\' branch" in withScalamuCompiler {
    (global, config) =>
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
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 4
  }
}