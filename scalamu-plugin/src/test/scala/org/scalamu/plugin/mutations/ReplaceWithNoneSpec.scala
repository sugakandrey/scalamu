package org.scalamu.plugin.mutations

import org.scalamu.plugin.{Mutation, SingleMutationSpec}

class ReplaceWithNoneSpec extends SingleMutationSpec {
  override def mutations: Seq[Mutation] = List(ReplaceWithNone)
  
  "ReplaceWithNone" should "replace Option.apply and Some.apply with None" in withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          |  Some(1).map(_ + 1)
          |  
          |  val none = Option(null).toList
          |  val two = Option[Int](1).fold(0)(_ + 1)
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 3
  }
}
