package org.scalamu.testutil
package fixtures

trait TestProjectFixture {
  def testProject: TestProject

  def withTestProject(
    code: TestProject => Any
  ): Any = code(testProject)
}

