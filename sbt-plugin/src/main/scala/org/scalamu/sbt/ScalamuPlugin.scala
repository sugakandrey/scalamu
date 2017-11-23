package org.scalamu.sbt

import org.scalamu.buildinfo.BuildInfo
import org.scalamu.sbt.Mutators.Mutator
import sbt.Def.Classpath
import sbt.{Def, Keys => K, _}
import sbt.plugins.JvmPlugin

import scala.util.matching.Regex

object ScalamuPlugin extends AutoPlugin {
  private[this] val organization = BuildInfo.scalamuOrganization
  private[this] val artifactId   = BuildInfo.scalamuName
  private[this] val version      = BuildInfo.scalamuVersion

  object autoImport extends ScalamuKeys {
    lazy val Scalamu: Configuration =
      config("scalamu")
        .describedAs("Dependencies and settings required for mutation testing.")
        .extend(Compile)
        .hide
  }

  import autoImport._

  override def requires: Plugins      = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override def projectConfigurations: Seq[Configuration] = Seq(Scalamu)

  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Scalamu)(Defaults.configSettings) ++
      Seq(
        scalamuTimeoutFactor       := 1.5,
        scalamuParallelism         := 1,
        scalamuTimeoutConst        := 2000,
        scalamuTargetOwners        := Seq.empty,
        scalamuTargetTests         := Seq.empty,
        scalamuIgnoreSymbols       := Seq.empty,
        scalamuEnabledMutators     := Mutators.enabledByDefault,
        scalamuAnalyserJavaOptions := (K.javaOptions in Test).value,
        scalamuVerbose             := false,
        scalamuRecompileOnly       := false,
        K.aggregate in scalamuRun  := false,
        K.aggregate in Scalamu     := true,
        K.target in Scalamu        := (K.target in Compile).value / "mutation-analysis-report",
        scalamuRun := {
          val report = K.update.value

          val scalamuJar = report.matching(
            configurationFilter(Scalamu.name) &&
              artifactFilter(`type` = "jar", name = "*scalamu*", classifier = "assembly")
          )
          assert(scalamuJar.size == 1, "Update report contains more than one scalamu-assembly jar.")

          val jar             = scalamuJar.head
          val scalamuVmParams = (K.javaOptions in Scalamu).value
          val log             = K.streams.value.log

          val tcp      = classpathIn(K.fullClasspath, Test).value
          val cp       = classpathIn(K.dependencyClasspath, Compile).value
          val sources  = sourceDirs.value.flatten.toSet
          val testDirs = testClassDirs.value.toSet

          val compilerPluginOpts = Classpaths.autoPlugins(report, Seq.empty)

          val runnerVmParams = scalamuAnalyserJavaOptions.value
          val factor         = scalamuTimeoutFactor.value
          val const          = scalamuTimeoutConst.value
          val verbose        = scalamuVerbose.value
          val recompileOnly  = scalamuRecompileOnly.value
          val parallelism    = scalamuParallelism.value
          val mutators       = scalamuEnabledMutators.value
          val ignored        = scalamuIgnoreSymbols.value
          val targetTests    = scalamuTargetTests.value
          val targetClasses  = scalamuTargetOwners.value

          val reportDir    = (K.target in Scalamu).value
          val testOptions  = K.testOptions.value
          val scalacParams = K.scalacOptions.value

          MutationTest(
            jar,
            scalamuVmParams,
            log,
            tcp,
            cp,
            sources,
            testDirs,
            reportDir,
            runnerVmParams,
            scalacParams ++ compilerPluginOpts,
            targetClasses,
            targetTests,
            ignored,
            mutators,
            testOptions,
            parallelism,
            factor,
            const,
            verbose,
            recompileOnly
          )
        }
      ) :+ dependencies

  def dependencies: Setting[Seq[ModuleID]] =
    K.libraryDependencies += (organization % s"${artifactId}_${K.scalaBinaryVersion.value}" % version % Scalamu)
      .classifier("assembly")
      .intransitive()

  private def aggregateSetting[T](
    settingKey: SettingKey[T],
    configurations: Configuration*
  ): Def.Initialize[Seq[T]] = Def.settingDyn {
    val projectRef          = K.thisProjectRef.value
    val aggregate           = (K.aggregate in Scalamu).value
    val projectFilter       = if (aggregate) inDependencies(projectRef) else inProjects(projectRef)
    val configurationFilter = if (configurations.isEmpty) inAnyConfiguration else inConfigurations(configurations: _*)
    val filter              = ScopeFilter(projectFilter, configurationFilter)
    settingKey.all(filter)
  }

  private def classpathIn(cpTask: TaskKey[Classpath], config: Configuration): Def.Initialize[Task[Seq[File]]] =
    (cpTask in config).map(
      cps => cps.collect { case entry if !entry.data.getPath.contains("org.scala-lang") => entry.data }
    )

  private lazy val sourceDirs: Def.Initialize[Seq[Seq[File]]] = aggregateSetting(K.sourceDirectories, Compile)
  private lazy val testClassDirs: Def.Initialize[Seq[File]]   = aggregateSetting(K.crossTarget)
}

object MutationTest {
  def apply(
    scalamuJar: File,
    scalamuVmParameters: Seq[String],
    log: Logger,
    tcp: Seq[File],
    cp: Seq[File],
    sources: Set[File],
    testDirs: Set[File],
    target: File,
    runnerVmParameters: Seq[String],
    scalacParameters: Seq[String],
    targetClasses: Seq[Regex],
    targetTests: Seq[Regex],
    ignoreSymbols: Seq[Regex],
    enabledMutators: Seq[Mutator],
    testOptions: Seq[TestOption],
    parallelism: Int,
    timeoutFactor: Double,
    timeoutConst: Long,
    verbose: Boolean,
    recompileOnly: Boolean
  ): File = {
    val arguments = scalamuArguments(
      tcp,
      cp,
      sources,
      testDirs,
      target,
      runnerVmParameters,
      scalacParameters,
      targetClasses,
      targetTests,
      ignoreSymbols,
      enabledMutators,
      testOptions,
      parallelism,
      timeoutFactor,
      timeoutConst,
      verbose,
      recompileOnly
    )

    val forkOptions = ForkOptions()
    val jarOpt      = Seq("-jar", s"${scalamuJar.getPath}")
    val exitCode    = Fork.java(forkOptions, scalamuVmParameters ++ jarOpt ++ arguments)

    exitCode match {
      case 0 => target
      case errorCode =>
        sys.error(
          s"Scalamu Runner failed with code: $errorCode." +
            s" You may want to turn verbose logging on to inspect the failure."
        )
    }
  }

  private def frameworkNames(maybeFramework: Option[TestFramework]): Seq[String] =
    maybeFramework match {
      case None                                                     => Seq("scalatest, specs2, utest, junit")
      case Some(framework) if framework == TestFrameworks.ScalaTest => Seq("scalatest")
      case Some(framework) if framework == TestFrameworks.JUnit     => Seq("junit")
      case Some(framework) if framework == TestFrameworks.Specs2    => Seq("specs2")
    }

  private def scalamuArguments(
    tcp: Seq[File],
    cp: Seq[File],
    sources: Set[File],
    testDirs: Set[File],
    target: File,
    vmParameters: Seq[String],
    scalacParameters: Seq[String],
    targetOwners: Seq[Regex],
    targetTests: Seq[Regex],
    ignoreSymbols: Seq[Regex],
    enabledMutators: Seq[Mutator],
    testOptions: Seq[TestOption],
    parallelism: Int,
    timeoutFactor: Double,
    timeoutConst: Long,
    verbose: Boolean,
    recompileOnly: Boolean
  ): Seq[String] = {

    val testRunnerArgs = testOptions
      .foldLeft(Map.empty[String, String]) {
        case (acc, Tests.Argument(framework, args)) =>
          val names = frameworkNames(framework)
          names.foldLeft(acc) {
            case (namesAcc, fname) =>
              val oldVal = namesAcc.getOrElse(fname, "")
              val updated =
                if (oldVal.isEmpty) args.mkString(" ")
                else
                  s"$oldVal ${args.mkString(" ")}"
              namesAcc + (fname -> updated)
          }
        case (acc, _) => acc
      }
      .map { case (fname, args) => s"$fname=$args" }
      .mkString(",")

    val possiblyUndefinedOptions = Seq(
      optionString(cp.map(_.getAbsolutePath), ",", "cp"),
      optionString(tcp.map(_.getAbsolutePath), ",", "tcp"),
      optionString(vmParameters, " ", "vmParameters"),
      optionString(targetOwners.map(_.toString), ",", "targetOwners"),
      optionString(targetTests.map(_.toString), ",", "targetTests"),
      optionString(scalacParameters, " ", "scalacParameters"),
      optionString(ignoreSymbols, ",", "ignoreSymbols"),
      optionString(enabledMutators, ",", "mutators")
    ).flatten

    val options = Seq(
      "--timeoutFactor",
      timeoutFactor.toString,
      "--timeoutConst",
      timeoutConst.toString,
      "--parallelism",
      parallelism.toString
    ) ++
      (if (verbose) Seq("--verbose")                                     else Seq.empty) ++
      (if (testRunnerArgs.nonEmpty) Seq("--testOptions", testRunnerArgs) else Seq.empty) ++
      (if (recompileOnly) Seq("--recompileOnly")                         else Seq.empty)

    val arguments = Seq(
      target.getAbsolutePath,
      sources.map(_.getAbsolutePath).mkString(","),
      testDirs.map(_.getAbsolutePath).mkString(",")
    )

    possiblyUndefinedOptions ++ options ++ arguments
  }

  private def optionString[T](
    options: Traversable[T],
    separator: String,
    name: String
  ): Seq[String] =
    if (options.isEmpty) Seq.empty
    else Seq(s"--$name", options.mkString(separator))
}
