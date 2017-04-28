package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceWithIdentityFunctionSpec extends SingleMutationSpec {

  override def mutation: Mutation = ReplaceWithIdentityFunction

  "ReplaceWithIdentityFunction" should "replace appropriately typed method calls and function literals with id" in
    withScalamuCompiler { (global, reporter) =>
      val code =
        """
          |object Foo {
          |  val xs = Set(1, 2, 3).map(1 + _)
          |  val opt = Some("123").map(s => s + "!")
          |  val ys = Set(3, 4, 5)
          |  
          |  val x = 123
          |  val a = (1 - x) - x
          |  
          |  (xs -- ys).foreach(println)
          |  
          |  List(1, 2, 3).map { x =>
          |    val zs = List(1, 2, 3).map(_ * 2)
          |    x + zs.sum
          |  }
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 8
    }

  it should "handle functions with multiple parameter lists, chained function calls and implicit parameters" ignore
    withScalamuCompiler { (global, reporter) =>
      val code =
        """
          |object Foo {
          |  val xs = Set(1, 2, 3).map(x => x.toString())
          |  
          |  class Bar() {
          |    def bar(i: Int)(s: String): Bar = this
          |  }
          |  
          |  val bar = new Bar()
          |  bar.bar(1)("123")
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 2
    }
}
