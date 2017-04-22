package org.scalamu.utils

import org.scalamu.testutil.{ScalamuSpec, TestProject}
import org.scalamu.testutil.fixtures.TestProjectFixture

import scala.util.Try

class ClassLoadingUtilsSpec extends ScalamuSpec with TestProjectFixture {
  override def testProject: TestProject = TestProject.simpleTestProject

  "ClassLoadingUtils" should "create a valid classloader from a target directory" in withTestProject {
    project =>
      val cl = loaderForPaths(project.target)
      val classes = Seq(
        "org.foo.bar.FizzBuzz",
        "org.foo.bar.FizzBuzz$",
        "org.baz.qux.FibsSpec",
        "org.baz.qux.FibsMicroTest$"
      )
      classes.foreach(c => Try(cl.loadClass(c)) should be a 'success)
  }

  it should "create a valid classloader from dependency jars" in withTestProject { project =>
    val cl = loaderForPaths(project.dependencies, None)
    val classes = Seq(
      "org.specs2.specification.core.SpecificationStructure",
      "org.specs2.control.eff.Eff",
      "org.scalatest.Status"
    )
    classes.foreach(c => Try(cl.loadClass(c)) should be a 'success)
  }
}
