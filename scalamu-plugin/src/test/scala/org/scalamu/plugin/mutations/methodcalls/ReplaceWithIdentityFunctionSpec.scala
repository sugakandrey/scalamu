package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.{Mutation, MutationOnlyRunner}
import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ReplaceWithIdentityFunctionSpec
    extends FlatSpec
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture {

  override def mutations: Seq[Mutation] = List(ReplaceWithIdentityFunction)

  "ReplaceWithIdentityFunction" should "replace method calls with type forall a. a => a with identity function" in withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          |  val xs = Set(1, 2, 3).map(1 + _)
          |  val opt = Some("123").map(s => s + "!")
          |  val ys = Set(3, 4, 5)
          |  
          |  val x = 123
          |  val a = 1 - x
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
      mutationsInfo should have size 10
    }
}
