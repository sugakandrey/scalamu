package org.scalamu.core.testapi

import org.scalamu.core.api.ClassName
import org.scalamu.testutil.ScalamuSpec
import org.scalamu.testutil.fixtures.TestProjectFixture

trait TestRunnerSpec extends ScalamuSpec with TestProjectFixture {
  protected def framework: TestingFramework

  protected def execute(suiteName: ClassName): TestSuiteResult =
    framework.runner.run(suiteName)

  protected def withTestProjectInClassPath(code: => Any): Any = withTestProject { project =>
    withContextClassLoader(loaderForPaths(project.target | project.dependencies)) {
      code
    }
  }
}
