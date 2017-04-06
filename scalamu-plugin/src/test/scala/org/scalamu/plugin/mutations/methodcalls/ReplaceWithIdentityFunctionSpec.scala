package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.util.SingleMutationSpec

class ReplaceWithIdentityFunctionSpec extends SingleMutationSpec {

  override def mutation: Mutation = ReplaceWithIdentityFunction

  "ReplaceWithIdentityFunction" should "replace appropriately typed method calls and function literals with id" in
    withScalamuCompiler { implicit global =>
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
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 8
    }

  it should "handle partially applied function, chained calls and implicit parameters" ignore withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          |  val xs = Set(1, 2, 3).map(x => x.toString())
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 1
  }
}
