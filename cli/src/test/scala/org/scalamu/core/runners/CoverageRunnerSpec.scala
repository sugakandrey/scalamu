package org.scalamu.core.runners

import java.io.File
import java.nio.file.{Path, Paths}

import org.scalamu.core.ClassName
import org.scalamu.core.compilation.{IsolatedScalamuGlobalFixture, ScalamuMutationPhase}
import org.scalamu.core.detection.SourceFileFinder
import org.scalamu.plugin.testutil.MutationTestRunner
import org.scalamu.testapi.{TestFailure, TestsFailed}
import org.scalamu.testutil.fixtures.{ScalamuConfigFixture, TestProjectFixture}
import org.scalamu.testutil.{ScalamuSpec, TestProject, TestingInstrumentationReporter}

import scala.reflect.io.{AbstractFile, Directory, PlainDirectory}
import scala.tools.nsc.Settings

class CoverageRunnerSpec
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
  override def scalaPath: String        = System.getenv("SCALA_HOME")
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

  "CoverageProcessSpec" should "calculate coverage in a separate JVM and send results to parent" in withConfig {
    config =>
      withScalamuGlobal { (global, _, instrumentation) =>
        val sources = new SourceFileFinder().findAll(Set(testProject.rootDir / "src" / "main"))
        global.withPhasesSkipped(ScalamuMutationPhase).compile(sources)

        val analyser = new CoverageAnalyser(
          config
            .copy(excludeTestsClasses = Seq(".*Bad.*".r)),
          global.outputDir.file.toPath
        )
        
        val coverage = analyser.analyse(instrumentation)
        coverage should have size 1
        forAll(coverage.values)(_.size should ===(11))
      }
  }

  it should "return info about failed test suites" in withConfig { config =>
    withScalamuGlobal { (global, _, instrumentation) =>
      val sources = new SourceFileFinder().findAll(Set(testProject.rootDir / "src" / "main"))
      global.withPhasesSkipped(ScalamuMutationPhase).compile(sources)

      val analyser = new CoverageAnalyser(
        config,
        global.outputDir.file.toPath
      )
      
      val failures = analyser.analyse(instrumentation)
      failures should have size 1
      failures should ===(
        List(
          TestsFailed(
            ClassName("org.example.failure.FooSpecBad"),
            Vector(
              TestFailure(
                "Test Foo should do bar() in suite FooSpecBad failed.",
                Some("-1 was not greater than 0")
              )
            )
          )
        )
      )
    }
  }
}
