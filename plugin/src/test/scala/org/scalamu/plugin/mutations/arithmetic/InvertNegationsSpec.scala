package org.scalamu.plugin.mutations.arithmetic

import org.scalamu.plugin.Mutation
import org.scalamu.plugin.testutil.SingleMutationSpec

class InvertNegationsSpec extends SingleMutationSpec {
  override def mutation: Mutation = InvertNegations

  "InvertNegations" should "mutate integer and floating point literals" in withScalamuCompiler {
    (global, config) =>
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
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 6
      mutantsInfo.map(mi => (mi.oldTree, mi.mutated)) should contain theSameElementsAs Seq(
        "-1123.0"      -> "1123.0",
        "-125"         -> "125",
        "-1.0E-10"     -> "1.0E-10",
        "-2147483648"  -> "2147483647",
        "-9147483648L" -> "9147483648L",
        "-2"           -> "2"
      )
  }

  it should "mutate other appropriately typed entities" in withScalamuCompiler {
    (global, config) =>
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
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 6
  }

  it should "behave correctly in the presence of type aliases" in withScalamuCompiler {
    (global, config) =>
      val code =
        """
          |object Foo {
          | type AnInt = Long
          | def foo(i: Int): AnInt = i
          | val long = -foo(10)
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, config.reporter)
      mutantsInfo should have size 1
      mutantsInfo.map(mi => (mi.oldTree, mi.mutated)) should contain(
        "Foo.this.foo(10).unary_-" -> "Foo.this.foo(10)"
      )
  }

  it should "not mutate unsupported types" in withScalamuCompiler { (global, config) =>
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
    val mutantsInfo = mutantsFor(code)(global, config.reporter)
    mutantsInfo shouldBe empty
  }
}