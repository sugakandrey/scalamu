package org.scalamu.core.coverage

import org.scalamock.scalatest.MockFactory
import org.scalamu.core.ClassName
import org.scalamu.testapi.TestSuiteResult._
import org.scalamu.testapi.{TestFailure, AbstractTestSuite, TestSuiteResult}
import org.scalamu.testutil.ScalamuSpec
import org.scalatest.OneInstancePerTest

class StatementCoverageAnalyzerSpec extends ScalamuSpec with OneInstancePerTest with MockFactory {
  private val reader   = mock[InvocationDataReader]
  private val reporter = mock[InstrumentationReporter]
  private val analyzer = new StatementCoverageAnalyzer(reader, reporter)

  private def mockSuite(expected: TestSuiteResult): AbstractTestSuite = {
    val suite = mock[AbstractTestSuite]
    (suite.execute _).expects().once().returning(expected)
    suite
  }

  "StatementCoverageAnalyzer" should "calculate coverage for a single successful test suite" in {
    val suiteName = ClassName("Foo")
    val suite     = mockSuite(Success(suiteName, 1000))
    val position  = Position(0, 10)

    (reader.invokedStatements _).expects().returning(Set(1, 2, 100, 1000)).once()
    (reporter.getStatementById _)
      .expects(*)
      .returning(Statement("Foo.scala", 1, position))
      .repeated(4)

    val expected = Map(
      suite -> Set(
        Statement("Foo.scala", 1, position),
        Statement("Foo.scala", 1, position),
        Statement("Foo.scala", 1, position),
        Statement("Foo.scala", 1, position)
      )
    )

    analyzer.forSuites(Set(suite)).value should ===(expected)
  }

  it should "calculate coverage for multiple successful suites" in {
    val suiteNames = List(ClassName("foo"), ClassName("bar"), ClassName("baz"))
    val suites     = suiteNames.map(name => mockSuite(Success(name, 1000)))

    val namesIterator = suiteNames.map(_.fullName).iterator
    val position      = Position(10, 20)

    (reader.invokedStatements _).expects().returning(Set(1)).repeated(3)
    (reporter.getStatementById _)
      .expects(*)
      .onCall((_: Int) => Statement(namesIterator.next + ".scala", 1, position))
      .repeated(3)

    val expected: Map[AbstractTestSuite, Set[Statement]] = suites
      .zip(suiteNames)
      .map {
        case (suite, name) =>
          suite -> Set(Statement(s"${name.fullName}.scala", 1, position))
      }(collection.breakOut)

    analyzer.forSuites(suites.toSet).value should ===(expected)
  }

  it should "aggregate failures in case some of the suites failed" in {
    val success         = ClassName("foo")
    val failures        = Seq(ClassName("bar"), ClassName("baz"), ClassName("qux"))
    val successfulSuite = mockSuite(Success(success, 10))
    val testFailure     = TestFailure("test 123 failed", None)
    val failedSuites =
      failures.map(name => mockSuite(TestsFailed(name, Seq(testFailure))))

    (reader.invokedStatements _).expects().returning(Set(1)).once()
    (reader.clearData _).expects().repeated(3)
    (reporter.getStatementById _)
      .expects(*)
      .returns(Statement("foo.scala", 1, Position(0, 10)))
      .once()

    analyzer.forSuites(Set(successfulSuite) | failedSuites.toSet).invalidValue.toList should ===(
      List(
        TestsFailed(ClassName("bar"), Seq(testFailure)),
        TestsFailed(ClassName("baz"), Seq(testFailure)),
        TestsFailed(ClassName("qux"), Seq(testFailure))
      )
    )
  }
}
