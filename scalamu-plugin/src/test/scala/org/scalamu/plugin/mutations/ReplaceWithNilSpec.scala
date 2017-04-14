package org.scalamu.plugin.mutations

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceWithNilSpec extends SingleMutationSpec {
  override def mutation: Mutation = ReplaceWithNil

  "ReplaceWithNil" should "replace List.apply calls with Nil" in withScalamuCompiler {
    (global, config) =>
      val code =
        """
          |object Foo {
          |  List(1, 2, 3).foreach(println)
          |  Set(1, 2, 3).foreach { x => }
          |  
          |  def foo[A](a: A): List[A] = List(a)
          |  
          |  class Bar(xs: List[String] = List("hello", "world"))
          |}
        """.stripMargin
      val mutationsInfo = mutantsFor(code)(global, config.reporter)
      mutationsInfo should have size 3
  }
}
