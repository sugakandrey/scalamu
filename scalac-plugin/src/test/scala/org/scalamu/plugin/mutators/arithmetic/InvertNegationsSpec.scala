package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.testutil.SingleMutationSpec

class InvertNegationsSpec extends SingleMutationSpec {
  override def mutation: Mutator = InvertNegations

  "InvertNegations" should "mutate integer and floating point literals" in withScalamuCompiler { (global, reporter) =>
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
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo should have size 6
  }

  it should "mutate other appropriately typed entities" in withScalamuCompiler { (global, reporter) =>
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
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo should have size 6
  }

  it should "behave correctly in the presence of type aliases" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        | type AnInt = Long
        | def foo(i: Int): AnInt = i
        | val long = -foo(10)
        |}
        """.stripMargin
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo should have size 1
  }

  it should "not mutate unsupported types" in withScalamuCompiler { (global, reporter) =>
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
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo shouldBe empty
  }
}
