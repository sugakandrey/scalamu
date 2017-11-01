package org.scalamu.testutil

import java.nio.file.{Path, Paths}

import org.scalamu.core.utils.FileSystemUtils._

final case class TestProject(
  name: String,
  rootDir: Path,
  dependencies: Set[Path],
  classes: Path,
  testClasses: Path
) {
  def target: Set[Path]    = Set(classes, testClasses)
  def classPath: Set[Path] = dependencies | target
}

object TestProject {
  private val testing: Path = Paths.get("../testing")

  val simpleTestProject: TestProject = inDir(testing / "simple")
  val JUnit: TestProject             = inDir(testing / "junit")
  val UTest: TestProject             = inDir(testing / "utest")
  val ScalaTest: TestProject         = inDir(testing / "scalatest")
  val Specs2: TestProject            = inDir(testing / "specs2")
  val Scoverage: TestProject         = inDir(testing / "scoverage")

  def inDir(dir: Path): TestProject = TestProject(
    dir.getFileName.toString,
    dir,
    (dir / "lib").filter(_.isJarOrZip).toSet,
    dir / "target" / "scala-2.12" / "classes",
    dir / "target" / "scala-2.12" / "test-classes"
  )
}
