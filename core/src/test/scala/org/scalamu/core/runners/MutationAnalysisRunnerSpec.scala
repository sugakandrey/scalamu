package org.scalamu.core.runners

import java.io.File
import java.nio.file.{Path, Paths}

import org.scalamu.core.compilation.IsolatedScalamuGlobalFixture
import org.scalamu.plugin.testutil.MutationTestRunner
import org.scalamu.testutil.fixtures.{ScalamuConfigFixture, TestProjectFixture}
import org.scalamu.testutil.{ScalamuSpec, TestProject, TestingInstrumentationReporter}

import scala.reflect.io.{AbstractFile, Directory, PlainDirectory}
import scala.tools.nsc.Settings

class MutationAnalysisRunnerSpec
    extends ScalamuSpec
    with MutationTestRunner
    with ScalamuConfigFixture
    with IsolatedScalamuGlobalFixture
    with TestProjectFixture {

  override def instrumentationReporter: TestingInstrumentationReporter =
    new TestingInstrumentationReporter

  override def testProject: TestProject = TestProject.Scoverage

  override def outputDir: AbstractFile = new PlainDirectory(
    new Directory(
      createTempDirectory("compiled").path.toFile
    )
  )

  override def testClassDirs: Set[Path] = Set(testProject.testClasses)

  override def classPath: Set[Path] =
    System
      .getProperty("java.class.path")
      .split(File.pathSeparator)
      .map(Paths.get(_))
      .toSet | testClassDirs

  override def spanScaleFactor: Double = 200.0

  override def createSettings(): Settings = new Settings {
    usejavacp.value = true
    outputDirs.setSingleOutput(outputDir)
    Yrangepos.value = true
  }

}
