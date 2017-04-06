package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.util.SingleMutationSpec

class RemoveUnitMethodCallsSpec extends SingleMutationSpec {

  override def mutation: Mutation = RemoveUnitMethodCalls

  "RemoveUnitMethodCalls" should "remove calls to methods returning Unit" in withScalamuCompiler {
    implicit global =>
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
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 5
  }
}
