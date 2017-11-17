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

  it should "not break compilation when inferred  type was Some[T]" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        |   class Foo(val foo: Option[Int], val bar: List[(Int, Int)])
        |
        |   private def getParams(p: List[(Foo, Int)]): List[Int] = {
        |    p.map(_._1).flatMap(pc => pc.foo :: pc.bar.map(p => Some(p._2))).flatten
        |  }
        |
        |  
        |}
        """.stripMargin
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo should have size 1
  }
}
