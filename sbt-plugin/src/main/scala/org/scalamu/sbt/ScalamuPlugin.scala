package org.scalamu.sbt

import sbt.{Def, Keys => K, _}
import sbt.plugins.JvmPlugin

import scala.util.matching.Regex

object ScalamuPlugin extends AutoPlugin {
  private[this] val organization = "io.github.sugakandrey"
  private[this] val artifactId   = "scalamu"
  private[this] val version      = "0.1.0"

  object autoImport extends ScalamuImport {
    lazy val Scalamu: Configuration =
      config("scalamu")
        .describedAs("Dependencies and settings required for mutation testing.")
        .extend(Compile)
        .hide
  }

  import autoImport.{ScalamuKeys => SK}
  import autoImport._

  override def requires: Plugins      = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override def projectConfigurations: Seq[Configuration] = Seq(Scalamu)

  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Scalamu)(Defaults.configSettings) ++
      Seq(
        SK.timeoutFactor               := 1.5,
        SK.parallelism                 := 1,
        SK.timeoutConst                := 2000,
        SK.targetOwners                := Seq.empty,
        SK.targetTests                 := Seq.empty,
        SK.ignoreSymbols               := Seq.empty,
        SK.activeMutators              := allMutators,
        SK.analyserJavaOptions         := (K.javaOptions in Test).value,
        SK.verbose                     := false,
        SK.recompileOnly               := false,
        K.aggregate in SK.mutationTest := false,
        K.aggregate in Scalamu         := true,
        K.target in Scalamu            := (K.target in Compile).value / "mutation-analysis-report",
        SK.mutationTest := {
          val report =
            K.update.value.matching(
              configurationFilter(Scalamu.name) &&
                artifactFilter(`type` = "jar", name = "*scalamu*", classifier = "assembly")
            )
          assert(report.size == 1, "Update report contains more than one scalamu-assembly jar.")

          val jar             = report.head
          val scalamuVmParams = (K.javaOptions in Scalamu).value
          val log             = K.streams.value.log

          val tcp      = testClassPath.value
          val cp       = compileClassPath.value
          val sources  = sourceDirs.value.flatten.toSet
          val testDirs = testClassDirs.value.toSet

          val runnerVmParams = SK.analyserJavaOptions.value
          val factor         = SK.timeoutFactor.value
          val const          = SK.timeoutConst.value
          val verbose        = SK.verbose.value
          val recompileOnly  = SK.recompileOnly.value
          val parallelism    = SK.parallelism.value
          val mutators       = SK.activeMutators.value
          val ignored        = SK.ignoreSymbols.value
          val targetTests    = SK.targetTests.value
          val targetClasses  = SK.targetOwners.value

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
            scalacParams,
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

  private def allMutators: Seq[String] = Seq(
    "ReplaceCaseWithWildcard",
    "ReplaceMathOperators",
    "ReplaceWithIdentityFunction",
    "InvertNegations",
    "AlwaysExecuteConditionals",
    "NeverExecuteConditionals",
    "ReplaceConditionalBoundaries",
    "NegateConditionals",
    "RemoveUnitMethodCalls",
    "ChangeRangeBoundary",
    "ReplaceLogicalOperators",
    "ReplaceWithNone",
    "ReplaceWithNil"
  )

  private def aggregateTask[T](
    taskKey: TaskKey[T],
    configurations: Configuration*
  ): Def.Initialize[Task[Seq[T]]] = Def.taskDyn {
    val projectRef          = K.thisProjectRef.value
    val aggregate           = (K.aggregate in Scalamu).value
    val projectFilter       = if (aggregate) inDependencies(projectRef) else inProjects(projectRef)
    val configurationFilter = if (configurations.isEmpty) inAnyConfiguration else inConfigurations(configurations: _*)
    val filter              = ScopeFilter(projectFilter, configurationFilter)
    taskKey.all(filter)
  }

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

  private def aggregateClassPath(config: Configuration): Def.Initialize[Task[Set[File]]] =
    aggregateTask(K.fullClasspath, config).map(
      cps =>
        cps.flatten.collect { case entry if !entry.data.getPath.contains("org.scala-lang") => entry.data }(
          collection.breakOut
      )
    )

  private lazy val testClassPath: Def.Initialize[Task[Set[File]]]    = aggregateClassPath(Test)
  private lazy val compileClassPath: Def.Initialize[Task[Set[File]]] = aggregateClassPath(Compile)
  private lazy val sourceDirs: Def.Initialize[Seq[Seq[File]]]        = aggregateSetting(K.sourceDirectories, Compile)
  private lazy val testClassDirs: Def.Initialize[Seq[File]]          = aggregateSetting(K.crossTarget)
}

object MutationTest {
  def apply(
    scalamuJar: File,
    scalamuVmParameters: Seq[String],
    log: Logger,
    tcp: Set[File],
    cp: Set[File],
    sources: Set[File],
    testDirs: Set[File],
    target: File,
    runnerVmParameters: Seq[String],
    scalacParameters: Seq[String],
    targetClasses: Seq[Regex],
    targetTests: Seq[Regex],
    ignoreSymbols: Seq[Regex],
    activeMutators: Seq[String],
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
      activeMutators,
      testOptions,
      parallelism,
      timeoutFactor,
      timeoutConst,
      verbose,
      recompileOnly
    )

    val forkOptions = ForkOptions()
    val jarOpt      = Seq("-jar", s"${scalamuJar.getPath}")
    val exitCode    = Fork.java(forkOptions, jarOpt ++ scalamuVmParameters ++ arguments)

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
    tcp: Set[File],
    cp: Set[File],
    sources: Set[File],
    testDirs: Set[File],
    target: File,
    vmParameters: Seq[String],
    scalacParameters: Seq[String],
    targetOwners: Seq[Regex],
    targetTests: Seq[Regex],
    ignoreSymbols: Seq[Regex],
    activeMutators: Seq[String],
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
      optionString(ignoreSymbols, ",", "ignoreSymbols")
    ).flatten

    val options = Seq(
      "--timeoutFactor",
      timeoutFactor.toString,
      "--timeoutConst",
      timeoutConst.toString,
      "--parallelism",
      parallelism.toString,
      "--mutators",
      activeMutators.mkString(",")
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
