package org.scalamu.core.coverage

import org.scalamock.scalatest.MockFactory
import org.scalamu.common.position.Position
import org.scalamu.plugin.MutantInfo
import org.scalamu.plugin.mutations.arithmetic.ReplaceMathOperators
import org.scalamu.testapi.AbstractTestSuite
import org.scalamu.testutil.ScalamuSpec

import scala.reflect.internal.util.{BatchSourceFile, RangePosition, SourceFile}

class InverseMutantCoverageSpec extends ScalamuSpec with MockFactory {

  private def createStubMutant(file: SourceFile, from: Int, to: Int): MutantInfo = MutantInfo(
    ReplaceMathOperators,
    1,
    new RangePosition(file, from, from, to),
    "",
    ""
  )

  "InverseMutantCoverage" should "calculate inverse mutant coverage from statement coverage" in {
    val suite1 = mock[AbstractTestSuite]
    val suite2 = mock[AbstractTestSuite]
    val suite3 = mock[AbstractTestSuite]

    val statementCoverage = Map(
      suite1 -> Set(
        Statement(1, 1, Position("Foo.scala", 10, 20)),
        Statement(2, 1, Position("Foo.scala", 100, 200)),
        Statement(3, 1, Position("Bar.scala", 0, 5)),
        Statement(4, 1, Position("Baz.scala", 998, 999))
      ),
      suite2 -> Set(
        Statement(5, 1, Position("Baz.scala", 998, 1009)),
        Statement(6, 1, Position("Bar.scala", 1, 5)),
        Statement(7, 1, Position("Qux.scala", 2, 5)),
        Statement(8, 1, Position("Foo.scala", 0, 100))
      ),
      suite3 -> Set(
        Statement(9, 1, Position("Baz.scala", 1000, 1010)),
        Statement(10, 1, Position("Foo.scala", 10, 210)),
        Statement(11, 1, Position("Qux.scala", 10, 210))
      )
    )

    val fooFile = new BatchSourceFile("Foo.scala", Seq())
    val barFile = new BatchSourceFile("Bar.scala", Seq())
    val bazFile = new BatchSourceFile("Baz.scala", Seq())
    val quxFile = new BatchSourceFile("Qux.scala", Seq())

    val (foo1, foo2, bar, baz, qux) = (
      createStubMutant(fooFile, 10, 19),
      createStubMutant(fooFile, 100, 101),
      createStubMutant(barFile, 3, 5),
      createStubMutant(bazFile, 1000, 1010),
      createStubMutant(quxFile, 1, 1)
    )

    val inverseMutantCoverage =
      InverseMutantCoverage.fromStatementCoverage(
        statementCoverage,
        Set(foo1, foo2, bar, baz, qux)
      )

    val expected = Map(
      foo1 -> Set(suite1, suite2, suite3),
      foo2 -> Set(suite1, suite3),
      bar  -> Set(suite1, suite2),
      baz  -> Set(suite3),
      qux  -> Set.empty
    )

    inverseMutantCoverage should ===(expected)
  }

}