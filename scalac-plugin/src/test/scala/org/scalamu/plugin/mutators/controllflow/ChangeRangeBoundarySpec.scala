package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.testutil.SingleMutationSpec

class ChangeRangeBoundarySpec extends SingleMutationSpec {
  override def mutation: Mutator = ChangeRangeBoundary

  "ChangeRangeBoundarySpec" should "replace \'until\' with \'to\' (and vice verse) for numerical types" in
    withScalamuCompiler { (global, reporter) =>
      val code =
        """
          |object Foo {
          |  (1 to 10).foreach(println)
          |  
          |  (-2d until -10d by -1).map(_ + 1)
          |  
          |  for {
          |    i <- 1l to 20l by 2
          |    j <- 1f until -1f by 0.5f
          |  } yield i * j
          |}
        """.stripMargin
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 4
    }

  it should "not support non-numeric types" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        |  case class Bar(i: Int) {
        |    def to(j: Int): Range = ???
        |  }
        |  
        |  val bar = Bar(123)
        |  (bar to 10).foreach(println)
        |}
        """.stripMargin
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo shouldBe empty
  }
}
