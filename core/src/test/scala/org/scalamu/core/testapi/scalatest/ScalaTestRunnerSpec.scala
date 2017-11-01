package org.scalamu.core.testapi.scalatest

import org.scalamu.core.testapi.{TestRunnerSpec, TestingFramework}
import org.scalamu.testutil.TestProject

class ScalaTestRunnerSpec extends TestRunnerSpec {
  override protected def framework: TestingFramework = ScalaTestFramework
  override def testProject: TestProject              = TestProject.ScalaTest

  "ScalaTestRunner" should "support different testing styles" in withTestProjectInClassPath {
    val suites = Seq(
      "org/scalamu/testing/scalatest/Flat.class",
      "org/scalamu/testing/scalatest/Free.class",
      "org/scalamu/testing/scalatest/Feature.class",
      "org/scalamu/testing/scalatest/FunSp.class",
      "org/scalamu/testing/scalatest/FunSu.class",
      "org/scalamu/testing/scalatest/Prop.class",
      "org/scalamu/testing/scalatest/Ref.class",
      "org/scalamu/testing/scalatest/Inherits.class",
      "org/scalamu/testing/scalatest/Word.class"
    ).map(classInfoForName)
    forAll(suites)(suite => execute(suite.name) should be a success)
  }

  it should "correctly report test failures" in withTestProjectInClassPath {
    val failedSuites = Seq(
      "org/scalamu/testing/scalatest/FailedFlat.class",
      "org/scalamu/testing/scalatest/FailedProp.class"
    ).map(classInfoForName)
    forAll(failedSuites)(suite => execute(suite.name) should be a failure)
  }

  it should "correctly report failures to instantiate a suite with TestSuiteResult.SuiteExecutionAborted" in withTestProjectInClassPath {
    val abortedSuite = classInfoForName("org/scalamu/testing/scalatest/WrapWithInvalid.class")
    execute(abortedSuite.name) shouldBe aborted
  }

  it should "support @WrapWith runners" in withTestProjectInClassPath {
    val wrapWithSuite = classInfoForName("org/scalamu/testing/scalatest/WrapWithValid.class")
    execute(wrapWithSuite.name) should be a success
  }

  it should "skip ignored tests" in withTestProjectInClassPath {
    val hasIgnoredSuite = classInfoForName("org/scalamu/testing/scalatest/HasIgnored.class")
    execute(hasIgnoredSuite.name) should be a success
  }
}
