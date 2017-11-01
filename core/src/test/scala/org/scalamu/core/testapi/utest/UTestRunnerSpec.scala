package org.scalamu.core.testapi.utest

import org.scalamu.core.testapi.{TestRunnerSpec, TestingFramework}
import org.scalamu.testutil.TestProject

class UTestRunnerSpec extends TestRunnerSpec {
  override protected def framework: TestingFramework = UTestFramework
  override def testProject: TestProject              = TestProject.UTest

  "UTestRunnerSpec" should "be able to run basic UTest classes" in withTestProjectInClassPath {
    val successfulTest = classInfoForName("org/scalamu/testing/utest/Successful.class")
    execute(successfulTest.name) should be a success
  }

  it should "correctly report failure of a test case" in withTestProjectInClassPath {
    val failedTest = classInfoForName("org/scalamu/testing/utest/Failed.class")
    execute(failedTest.name) should be a failure
  }

  it should "report failure to load suite class with TestSuiteResult.Abort" in withTestProjectInClassPath {
    val suite = classInfoForName("org/scalamu/testing/utest/Successful.class")
    Thread
      .currentThread()
      .setContextClassLoader(getClass.getClassLoader) // unset test project classloader
    execute(suite.name) shouldBe aborted
  }

  it should "support utest retry capabilities" in withTestProjectInClassPath {
    val runWithJarSuite = classInfoForName("org/scalamu/testing/utest/Retries.class")
    execute(runWithJarSuite.name) should be a success
  }
}
