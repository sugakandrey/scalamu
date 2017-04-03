package org.scalamu.plugin.mutations

import org.scalamu.plugin.{Mutation, SingleMutationSpec}

class ReplaceWithNilSpec extends SingleMutationSpec {
  override def mutations: Seq[Mutation] = List(ReplaceWithNil)
  
  "ReplaceWithNil" should "replace List.apply calls with Nil" in withScalamuCompiler {
    implicit global =>
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
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 3
  }
}
