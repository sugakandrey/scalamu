package org.scalamu.core.runners

import java.io.File
import java.nio.file.Path

import org.scalamu.core.compilation.{IsolatedScalamuGlobalFixture, ScalamuMutationPhase}
import org.scalamu.core.detection.SourceFileFinder
import org.scalamu.plugin.testutil.MutationTestRunner
import org.scalamu.plugin.{Mutation, ScalamuPluginConfig}
import org.scalamu.testutil.fixtures.{ScalamuConfigFixture, TestProjectFixture}
import org.scalamu.testutil.{ScalamuSpec, TestProject, TestingInstrumentationReporter}

import scala.reflect.io.{AbstractFile, Directory, PlainDirectory}
import scala.tools.nsc.Settings

class CoverageRunnerSpec
    extends ScalamuSpec
    with ScalamuConfigFixture
    with MutationTestRunner
    with IsolatedScalamuGlobalFixture
    with TestProjectFixture {

  override def instrumentationReporter: TestingInstrumentationReporter =
    new TestingInstrumentationReporter

  override def mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations
  override def testProject: TestProject = TestProject.Scoverage
  override def testClassDirs: Set[Path] = Set(testProject.testClasses)

  override def outputDir: AbstractFile = new PlainDirectory(
    new Directory(
      createTempDirectory("compiled").path.toFile
    )
  )

  override def createSettings(): Settings = new Settings {
    usejavacp.value = true
    classpath.value += classPath.fold("")(_ + File.pathSeparator + _)
    outputDirs.setSingleOutput(outputDir)
    Yrangepos.value = true
  }

  "CoverageRunner" should "calculate coverage of a single successful test suite" in withConfig {
    config =>
      withScalamuGlobal { (global, _, instrumentation) =>
        val sources = new SourceFileFinder().findAll(Set(testProject.rootDir / "src" / "main"))
        global.withPhasesSkipped(ScalamuMutationPhase).compile(sources)
        val compiledSourcesPath = global.settings.outputDirs.getSingleOutput.get.file.toPath

        withContextClassLoader(Set(testProject.testClasses, compiledSourcesPath)) {
          val coverage = CoverageRunner.run(
            config.copy(excludeTestsClasses = Seq(".*Bad.*".r)),
            instrumentation
          )
          val suiteCoverage = coverage.right.value
          suiteCoverage should have size 1
          forAll(suiteCoverage.values)(_.size should ===(11))
        }
      }
  }

  it should "return ScalamuFailure if aborted or failed tests were present" in withConfig {
    config =>
      withScalamuGlobal { (global, _, instrumentation) =>
        val sources = new SourceFileFinder().findAll(Set(testProject.rootDir / "src" / "main"))
        global.withPhasesSkipped(ScalamuMutationPhase).compile(sources)
        val compiledSourcesPath = global.settings.outputDirs.getSingleOutput.get.file.toPath

        withContextClassLoader(Set(testProject.testClasses, compiledSourcesPath)) {
          val coverage = CoverageRunner.run(
            config,
            instrumentation
          )
          coverage.left.value.tests.toList should have size 1
        }
      }
  }

}
