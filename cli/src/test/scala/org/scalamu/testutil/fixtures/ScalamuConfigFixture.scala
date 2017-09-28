package org.scalamu.testutil.fixtures

import java.nio.file.{Path, Paths}

import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.plugin.{Mutator, ScalamuPluginConfig}
import org.scalatest.TestSuite

import scala.util.matching.Regex

trait ScalamuConfigFixture extends TestSuite {
  def reportDir: Path                     = Paths.get(".")
  def sourceDirs: Set[Path]               = Set.empty
  def testClassDirs: Set[Path]            = Set.empty
  def classPath: Set[Path]                = Set.empty
  def testClassPath: Set[Path]            = Set.empty
  def vmParameters: String                = ""
  def mutations: Seq[Mutator]             = ScalamuPluginConfig.allMutators
  def targetSources: Seq[Regex]           = Seq.empty
  def targetTests: Seq[Regex]             = Seq.empty
  def ignoreSymbols: Seq[Regex]           = Seq.empty
  def testingOptions: Map[String, String] = Map.empty
  def scalacParameters: String            = ""
  def timeoutFactor: Double               = 1.5
  def timeoutConst: Long                  = 2000
  def threads: Int                        = Runtime.getRuntime.availableProcessors()
  def verbose: Boolean                    = false
  def recompileOnly: Boolean              = false

  def withConfig(code: ScalamuConfig => Any): Any =
    code(
      ScalamuConfig(
        reportDir,
        sourceDirs,
        testClassDirs,
        classPath,
        testClassPath,
        vmParameters,
        mutations,
        targetSources,
        targetTests,
        ignoreSymbols,
        testingOptions,
        scalacParameters,
        timeoutFactor,
        timeoutConst,
        threads,
        verbose,
        recompileOnly
      )
    )
}
