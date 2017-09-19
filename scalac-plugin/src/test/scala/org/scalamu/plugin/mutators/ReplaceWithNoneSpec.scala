package org.scalamu.plugin.mutators

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceWithNoneSpec extends SingleMutationSpec {
  override def mutation: Mutator = ReplaceWithNone

  "ReplaceWithNone" should "replace Option.apply and Some.apply with None" in withScalamuCompiler {
    (global, reporter) =>
      val code =
        """
          |object Foo {
          |  Some(1).map(_ + 1)
          |  
          |  val none = Option(null).toList
          |  val two = Option[Int](1).fold(0)(_ + 1)
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 3
  }
}
