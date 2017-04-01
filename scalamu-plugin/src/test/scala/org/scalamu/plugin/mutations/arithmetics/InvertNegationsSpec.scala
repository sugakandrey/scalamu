package org.scalamu.plugin.mutations.arithmetics

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalamu.plugin.{Mutation, MutationOnlyRunner}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class InvertNegationsSpec
    extends FlatSpec
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture {

  override def mutations: Seq[Mutation] = List(InvertNegations)

  "InvertNegations" should "mutate integer and floating point literals" in withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          |  val float = -1123f
          |  
          |  val short: Short = -125
          |  
          |  val double = -1e-10
          |  
          |  println(-2147483648)
          |  
          |  def long = -9147483648l + foo()
          | 
          |  def foo(): Byte = -2
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 6
      mutationsInfo.map(mi => (mi.oldTree, mi.mutated)) should contain theSameElementsAs Seq(
        "-1123.0"      -> "1123.0",
        "-125"         -> "125",
        "-1.0E-10"     -> "1.0E-10",
        "-2147483648"  -> "2147483647",
        "-9147483648L" -> "9147483648L",
        "-2"           -> "2"
      )
  }

  it should "mutate other appropriately typed entities" in withScalamuCompiler { implicit global =>
    val code =
      """
        |class Bar[T](val polyParam: T) {
        |  val field: Long = Long.MaxValue
        |}
        |
        |class Foo(val param: Float) {
        |  val bar = new Bar[Byte](5)
        |  
        |  def foo(i: Float): Double = {
        |    println(-i)
        |    i
        |  }
        | 
        |  val b = -foo(-param)
        | 
        |  val c = -bar.field
        | 
        |  def poly[A](a: A): A = a
        |  
        |  val g = -poly(2f) + (-bar.polyParam)
        |}
        """.stripMargin
    val mutationsInfo = mutationsFor(code)
    mutationsInfo should have size 6
  }

  it should "behave correctly in the presence of type aliases" in withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          | type AnInt = Long
          | def foo(i: Int): AnInt = i
          | val long = -foo(10)
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 1
      mutationsInfo.map(mi => (mi.oldTree, mi.mutated)) should contain(
        "Foo.this.foo(10).unary_-" -> "Foo.this.foo(10)"
      )
  }

  it should "not mutate unsupported types" in withScalamuCompiler { implicit global =>
    val code =
      """
        |object Foo {
        |  case class A(val i: Int) {
        |    def unary_-(): A = A(0)
        |  }
        |  
        |  val c = -A(2)
        |}
      """.stripMargin
    val mutationsInfo = mutationsFor(code)
    mutationsInfo.shouldBe(empty)
  }
}
