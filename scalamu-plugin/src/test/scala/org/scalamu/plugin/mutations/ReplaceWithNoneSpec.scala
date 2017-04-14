package org.scalamu.plugin.mutations

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceWithNoneSpec extends SingleMutationSpec {
  override def mutation: Mutation = ReplaceWithNone

  "ReplaceWithNone" should "replace Option.apply and Some.apply with None" in withScalamuCompiler {
    (global, config) =>
      val code =
        """
          |object Foo {
          |  Some(1).map(_ + 1)
          |  
          |  val none = Option(null).toList
          |  val two = Option[Int](1).fold(0)(_ + 1)
          |}
        """.stripMargin
      val mutationsInfo = mutantsFor(code)(global, config.reporter)
      mutationsInfo should have size 3
  }
}
