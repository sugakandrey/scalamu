package org.scalamu.core.testapi.junit

import org.scalamu.core.testapi.{TestRunnerSpec, TestingFramework}
import org.scalamu.testutil.TestProject

class JUnitRunnerSpec extends TestRunnerSpec {
  override protected def framework: TestingFramework = JUnitFramework
  override def testProject: TestProject              = TestProject.JUnit

  "JUnitRunner" should "be able to run basic JUnit 4 test classes" in withTestProjectInClassPath {
    val successfulTest = classInfoForName("org/scalamu/testing/junit/Successful.class")
    execute(successfulTest.name) should be a success
  }

  it should "be able to run JUnit 4 test classes with fixtures" in withTestProjectInClassPath {
    val hasFixtureSuite = classInfoForName("org/scalamu/testing/junit/HasFixture.class")
    execute(hasFixtureSuite.name) should be a success
  }

  it should "correctly report failure of a test case" in withTestProjectInClassPath {
    val failedTest = classInfoForName("org/scalamu/testing/junit/Failed.class")
    execute(failedTest.name) should be a failure
  }

  it should "correctly report failure to run a suite" in withTestProjectInClassPath {
    val abortedSuite = classInfoForName("org/scalamu/testing/junit/NotStatic.class")
    execute(abortedSuite.name) should be a failure
  }

  it should "report failure to load suite class with TestSuiteResult.Abort" in withTestProjectInClassPath {
    val suite = classInfoForName("org/scalamu/testing/junit/Successful.class")
    Thread
      .currentThread()
      .setContextClassLoader(getClass.getClassLoader) // unset test project classloader
    execute(suite.name) shouldBe aborted
  }

  it should "be able to run test suites with custom runners" in withTestProjectInClassPath {
    val runWithSuite = classInfoForName("org/scalamu/testing/junit/RunWithParam.class")
    execute(runWithSuite.name) should be a success
  }

  it should "be able to run test suites with custom runners from jar deps" in withTestProjectInClassPath {
    val runWithJarSuite = classInfoForName("org/scalamu/testing/junit/RunWithJar.class")
    execute(runWithJarSuite.name) should be a success
  }

  it should "be able to run test suites with @RunWith annotation inherited from superclass" in withTestProjectInClassPath {
    val runWithJarSuite = classInfoForName("org/scalamu/testing/junit/RunWithInherited.class")
    execute(runWithJarSuite.name) should be a success
  }
}
