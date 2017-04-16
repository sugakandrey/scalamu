package org.scalamu.core.fixtures

import org.scalamu.core.TestProject

trait TestProjectFixture {
  def testProject: TestProject
  
  def withTestProject(
    code: TestProject => Any
  ): Any = code(testProject)
}

