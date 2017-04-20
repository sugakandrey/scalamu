package org.scalamu.testapi

import org.scalamu.testapi.junit.JUnitFramework
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework
import org.scalamu.testutil.{FrameworkSupport, ScalamuSpec, TestProject}
import org.scalamu.testutil.fixtures.TestProjectFixture

class TestClassFinderSpec extends ScalamuSpec with TestProjectFixture with FrameworkSupport {
  override def testProject: TestProject = TestProject.simpleTestProject

  "TestClassFinder" should "detect class files, which contain ScalaTest tests" in withTestProject {
    project =>
      val finder = new TestClassFileFinder(ScalaTestFramework.filter)
      val classFiles = withContextClassLoader(loaderForPaths(project.dependencies))(
        finder.findAll(project.target)
      )
      classFiles should have size 2
      classFiles.map(_.info.name.fullName) should contain theSameElementsAs Seq(
        "org.baz.qux.FibsSpec",
        "org.foo.bar.FizzBuzzSpec"
      )
  }

  it should "detect class files, which contain UTest tests" in withTestProject { project =>
    val finder = new TestClassFileFinder(UTestFramework.filter)
    val classFiles = withContextClassLoader(loaderForPaths(project.dependencies))(
      finder.findAll(project.target)
    )
    classFiles should have size 1
    classFiles.map(_.info.name.fullName) should contain theSameElementsAs Seq(
      "org.baz.qux.FibsMicroTest"
    )
  }

  it should "detect class files, which contain JUnit tests" in withTestProject { project =>
    val finder = new TestClassFileFinder(JUnitFramework.filter)
    val classFiles = withContextClassLoader(loaderForPaths(project.dependencies))(
      finder.findAll(project.target)
    )
    classFiles should have size 2
    classFiles.map(_.info.name.fullName) should contain theSameElementsAs Seq(
      "org.baz.qux.FibsTest",
      "org.foo.bar.FizzBuzzTest"
    )
  }

  it should "detect class files, which contain Specs2 tests" in withTestProject { project =>
    val finder = new TestClassFileFinder(Specs2Framework.filter)
    val classFiles = withContextClassLoader(loaderForPaths(project.dependencies))(
      finder.findAll(project.target)
    )
    classFiles should have size 1
    classFiles.map(_.info.name.fullName) should contain theSameElementsAs Seq(
      "org.baz.qux.FibsSpecs2"
    )
  }

  it should "detect class files which contain tests" in withTestProject { project =>
    val finder = new TestClassFileFinder(filter)
    val classFiles = withContextClassLoader(loaderForPaths(project.dependencies))(
      finder.findAll(project.target)
    )
    classFiles should have size 6
    val fizzBuzzPart = Seq(
      "org.foo.bar.FizzBuzzSpec",
      "org.foo.bar.FizzBuzzTest"
    )
    val fibsPart = Seq(
      "org.baz.qux.FibsSpec",
      "org.baz.qux.FibsTest",
      "org.baz.qux.FibsSpecs2",
      "org.baz.qux.FibsMicroTest"
    )
    (classFiles.map(_.info.name.fullName) should contain).allElementsOf(fizzBuzzPart)
    (classFiles.map(_.info.name.fullName) should contain).allElementsOf(fibsPart)
  }

}
