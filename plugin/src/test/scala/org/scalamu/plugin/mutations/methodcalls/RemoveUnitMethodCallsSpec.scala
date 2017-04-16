package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class RemoveUnitMethodCallsSpec extends SingleMutationSpec {

  override def mutation: Mutation = RemoveUnitMethodCalls

  "RemoveUnitMethodCalls" should "remove calls to methods returning Unit" in withScalamuCompiler {
    (global, config) =>
      val code =
        """
          |object Foo {
          |  println(1)
          |  
          |  def foo[A](a: A): Unit = ???
          |  
          |  List(1, 2, 3).foreach(println)
          |  
          |  var mut = 0
          |  Some(1).map(x => mut += x)
          |  
          |  foo("123")
          |  
          |  def poly[A](a: A): Unit = ???
          |  
          |  poly[String]("123")
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 6
  }

  it should "handle partially functions with multiple parameter lists and implicit parameters" ignore
    withScalamuCompiler { (global, config) =>
      val code =
        """
          |object Foo {
          |  def bar(s: String)(implicit i: Int): Unit = ???
          |  
          |  implicit val i: Int = 123
          |  bar("123")
          |  
          |  def baz(i: Int)(j: Int)(k: Int): Unit = ???
          |  
          |  baz(1)(2)(3)
          |  
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 2
    }
}
