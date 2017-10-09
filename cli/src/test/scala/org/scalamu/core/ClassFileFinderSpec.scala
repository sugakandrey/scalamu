package org.scalamu.core

import org.scalamu.core.detection.ClassFileFinder
import org.scalamu.testutil.fixtures.TestProjectFixture
import org.scalamu.testutil.{ScalamuSpec, TestProject}

class ClassFileFinderSpec extends ScalamuSpec with TestProjectFixture {

  override def testProject: TestProject = TestProject.simpleTestProject

  "ClassFileFinder" should "find and parse ClassFilesInfo given the correct paths" in withTestProject { project =>
    val finder = new ClassFileFinder
    val classFiles = withContextClassLoader(loaderForPaths(project.dependencies))(
      finder.findAll(project.target)
    )
    classFiles should have size 11
    val fizzBuzzPart = Seq(
      "org.foo.bar.FizzBuzz",
      "org.foo.bar.FizzBuzz$",
      "org.foo.bar.FizzBuzzSpec",
      "org.foo.bar.FizzBuzzTest"
    )
    val fibsPart = Seq(
      "org.baz.qux.Fibs",
      "org.baz.qux.Fibs$",
      "org.baz.qux.FibsSpec",
      "org.baz.qux.FibsTest",
      "org.baz.qux.FibsSpecs2",
      "org.baz.qux.FibsMicroTest$",
      "org.baz.qux.FibsMicroTest"
    )
    classFiles.map(_.name.fullName) should contain allElementsOf fizzBuzzPart
    classFiles.map(_.name.fullName) should contain allElementsOf fibsPart
  }
}
