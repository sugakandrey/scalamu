package org.scalamu.plugin.mutators

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceWithNilSpec extends SingleMutationSpec {
  override def mutation: Mutator = ReplaceWithNil

  "ReplaceWithNil" should "replace List.apply calls with Nil" in withScalamuCompiler {
    (global, reporter) =>
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
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 3
  }
}
