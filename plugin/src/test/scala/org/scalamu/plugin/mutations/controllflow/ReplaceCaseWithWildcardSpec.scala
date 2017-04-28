package org.scalamu.plugin.mutations.controllflow

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceCaseWithWildcardSpec extends SingleMutationSpec {
  override def mutation: Mutation = ReplaceCaseWithWildcard

  "ReplaceCaseWithWildcard" should "replace case expressions with wildcard (if one is present)" in withScalamuCompiler {
    (global, reporter) =>
      val code =
        """
          |object Foo {
          |  val cond = false
          |  123 match {
          |    case 1 => println("not wild card")
          |    case _ => 
          |  }
          | 
          |  val x = Some("Hello") match {
          |    case Some(s @ "Hello") => s + "World!"
          |    case s @ _  => s
          |  }
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 2
  }

  it should "do nothing if wildcard patter is restricted with guard or type ascription" in withScalamuCompiler {
    (global, reporter) =>
      val code =
        """
          |object Foo {
          |  val a: Any = 123
          |  a match {
          |    case _: Double => 0.0d
          |    case _: Int => 0
          |  }
          |  
          |  Some("Hello") match {
          |    case _ if false => 1
          |    case _ if true => 2
          |  }
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo shouldBe empty
  }
}
