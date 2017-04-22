package org.scalamu.testapi.junit

import org.scalamu.core.ClassName
import org.scalamu.testapi.TestSuiteResult
import org.scalamu.testutil.{ScalamuSpec, TestProject}
import org.scalamu.testutil.fixtures.TestProjectFixture

class JUnitRunnerSpec extends ScalamuSpec with TestProjectFixture {
  override def testProject: TestProject = TestProject.JUnit

  private def execute(suiteName: ClassName): TestSuiteResult =
    JUnitFramework.runner.run(suiteName)

  private def withTestProjectInClassPath(code: TestProject => Any): Any = withTestProject {
    project =>
      withContextClassLoader(loaderForPaths(project.target | project.dependencies)) {
        code(project)
      }
  }

  "JUnitRunner" should "be able to run basic JUnit 4 test classes" in withTestProjectInClassPath {
    project =>
      val successfulTest = classInfoForName("org/scalamu/testing/junit/Successful.class")
      execute(successfulTest.name) should be a success
  }

  it should "be able to run JUnit 4 test classes with fixtures" in withTestProjectInClassPath {
    project =>
      val hasFixtureSuite = classInfoForName("org/scalamu/testing/junit/HasFixture.class")
      execute(hasFixtureSuite.name) should be a success
  }

  it should "correctly report failure of a test case" in withTestProjectInClassPath { project =>
    val failedTest = classInfoForName("org/scalamu/testing/junit/Failed.class")
    execute(failedTest.name) should be a failure
  }

  it should "correctly report failure to run a suite" in withTestProjectInClassPath { project =>
    val abortedSuite = classInfoForName("org/scalamu/testing/junit/NotStatic.class")
    execute(abortedSuite.name) should be a failure
  }

  it should "report failure to load suite class with TestSuiteResult.Abort" in withTestProjectInClassPath {
    _ =>
      val suite = classInfoForName("org/scalamu/testing/junit/Successful.class")
      Thread.currentThread().setContextClassLoader(getClass.getClassLoader) // unset test project classloader
      execute(suite.name) shouldBe aborted
  }

  it should "be able to run test suites with custom runners" in withTestProjectInClassPath {
    project =>
      val runWithSuite = classInfoForName("org/scalamu/testing/junit/RunWithParam.class")
      execute(runWithSuite.name) should be a success
  }

  it should "be able to run test suites with custom runners from jar deps" in withTestProjectInClassPath {
    project =>
      val runWithJarSuite = classInfoForName("org/scalamu/testing/junit/RunWithJar.class")
      execute(runWithJarSuite.name) should be a success
  }

  it should "be able to run test suites with @RunWith annotation inherited from superclass" in withTestProjectInClassPath {
    project =>
      val runWithJarSuite = classInfoForName("org/scalamu/testing/junit/RunWithInherited.class")
      execute(runWithJarSuite.name) should be a success
  }
}
