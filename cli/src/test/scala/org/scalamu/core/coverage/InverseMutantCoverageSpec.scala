package org.scalamu.core.coverage

import org.scalamock.scalatest.MockFactory
import org.scalamu.common.position.Position
import org.scalamu.core.process.MeasuredSuite
import org.scalamu.plugin.MutantInfo
import org.scalamu.plugin.mutators.arithmetic.ReplaceMathOperators
import org.scalamu.testapi.AbstractTestSuite
import org.scalamu.testutil.ScalamuSpec
import scoverage.Location

import scala.reflect.internal.util.{BatchSourceFile, RangePosition, SourceFile}

class InverseMutantCoverageSpec extends ScalamuSpec with MockFactory {

  private def createStubMutant(file: SourceFile, line: Int, from: Int, to: Int): MutantInfo =
    MutantInfo(
      ReplaceMathOperators,
      1,
      "org.example",
      new StubPosition(file, line, from, to),
      "",
      ""
    )

  private class StubPosition(source: SourceFile, override val line: Int, from: Int, to: Int)
      extends RangePosition(source, from, from, to)

  "InverseMutantCoverage" should "calculate inverse mutant coverage from statement coverage" in {
    val suite1   = mock[AbstractTestSuite]
    val suite2   = mock[AbstractTestSuite]
    val suite3   = mock[AbstractTestSuite]
    val location = mock[Location]

    val statementCoverage = Map(
      MeasuredSuite(suite1, 0) -> Set(
        Statement(StatementId(1), location, Position("Foo.scala", 1, 10, 20)),
        Statement(StatementId(2), location, Position("Foo.scala", 1, 100, 200)),
        Statement(StatementId(3), location, Position("Bar.scala", 1, 0, 5)),
        Statement(StatementId(4), location, Position("Baz.scala", 1, 998, 999))
      ),
      MeasuredSuite(suite2, 0) -> Set(
        Statement(StatementId(5), location, Position("Baz.scala", 1, 998, 1009)),
        Statement(StatementId(6), location, Position("Bar.scala", 1, 1, 5)),
        Statement(StatementId(7), location, Position("Qux.scala", 1, 2, 5)),
        Statement(StatementId(8), location, Position("Foo.scala", 1, 0, 99))
      ),
      MeasuredSuite(suite3, 0) -> Set(
        Statement(StatementId(9), location, Position("Baz.scala", 1, 1000, 1010)),
        Statement(StatementId(10), location, Position("Foo.scala", 1, 10, 210)),
        Statement(StatementId(11), location, Position("Qux.scala", 1, 10, 210))
      )
    )

    val fooFile = new BatchSourceFile("Foo.scala", Seq())
    val barFile = new BatchSourceFile("Bar.scala", Seq())
    val bazFile = new BatchSourceFile("Baz.scala", Seq())
    val quxFile = new BatchSourceFile("Qux.scala", Seq())

    val (foo1, foo2, bar, baz, qux) = (
      createStubMutant(fooFile, 1, 10, 19),
      createStubMutant(fooFile, 1, 100, 101),
      createStubMutant(barFile, 1, 3, 5),
      createStubMutant(bazFile, 1, 1000, 1010),
      createStubMutant(quxFile, 1, 1, 1)
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
      baz  -> Set(suite3, suite2),
      qux  -> Set.empty
    )

    inverseMutantCoverage should ===(expected)
  }

}
