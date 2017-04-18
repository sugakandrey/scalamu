package org.scalamu.testutil

import java.nio.file.{Path, Paths}

import org.scalamu.utils.FileSystemUtils._

final case class TestProject(
  name: String,
  rootDir: Path,
  dependencies: Set[Path],
  target: Path
) {
  def classPath: Set[Path] = dependencies + target
}

object TestProject {
  private val testing: Path = Paths.get("../testing")
  
  val simpleTestProject: TestProject = inDir(testing / "simple")
  
  def inDir(dir: Path): TestProject = TestProject(
    dir.getFileName.toString,
    dir,
    (dir / "lib").filter(_.isJarOrZip).toSet,
    dir / "target" / "scala-2.12"
  )
}
