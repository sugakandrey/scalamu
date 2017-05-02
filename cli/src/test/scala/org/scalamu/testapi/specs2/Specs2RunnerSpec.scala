package org.scalamu.testapi.specs2

import org.scalamu.testapi.{TestRunnerSpec, TestingFramework}
import org.scalamu.testutil.TestProject

class Specs2RunnerSpec extends TestRunnerSpec {
  override protected def framework: TestingFramework = Specs2Framework
  override def testProject: TestProject              = TestProject.Specs2

  "Specs2Runner" should "support different testing styles" in withTestProjectInClassPath {
    val suites = Seq(
      "org/scalamu/testing/specs2/Acceptance.class",
      "org/scalamu/testing/specs2/HasFixture.class",
      "org/scalamu/testing/specs2/UnitSpec.class"
    ).map(classInfoForName)
    forAll(suites)(suite => execute(suite.name) should be a success)
  }

  it should "correctly report test failures" in withTestProjectInClassPath {
    val failedSuite = classInfoForName("org/scalamu/testing/specs2/Failed.class")
    execute(failedSuite.name) should be a failure
  }

  it should "correctly report failures to instantiate a suite with TestSuiteResult.SuiteExecutionAborted" in withTestProjectInClassPath {
    val abortedSuite = classInfoForName("org/scalamu/testing/specs2/Acceptance.class")
    Thread
      .currentThread()
      .setContextClassLoader(getClass.getClassLoader) // unset test project classloader
    execute(abortedSuite.name) shouldBe aborted
  }

  it should "skip ignored tests" in withTestProjectInClassPath {
    val hasIgnoredSuite = classInfoForName("org/scalamu/testing/specs2/HasIgnored.class")
    execute(hasIgnoredSuite.name) should be a success
  }
}
