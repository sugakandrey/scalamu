package org.scalamu.sbt

import sbt.{Def, Keys => K, _}
import sbt.plugins.JvmPlugin

object ScalamuPlugin extends AutoPlugin {
  private[this] val organization = "io.github.sugakandrey"
  private[this] val artifactId   = "scalamu-assembly"
  private[this] val version      = "0.1-SNAPSHOT"
  private[this] val mainClass    = "org.scalamu.entry.EntryPoint"

  object autoImport extends ScalamuImport {
    lazy val MutationTest: Configuration =
      config("mutation-test").describedAs("Dependencies and settings required for mutation testing.").hide
  }

  import autoImport.{ScalamuKeys => SK}
  import autoImport._

  override def requires: Plugins      = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override def projectConfigurations: Seq[Configuration] = Seq(MutationTest)

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      K.aggregate in SK.mutationTest := false,
      SK.mutationTest                := mutationTestTask.value,
      SK.timeoutFactor               := 1.5,
      SK.parallelism                 := 1,
      SK.timeoutConst                := 2000,
      SK.targetClasses               := Seq.empty,
      SK.targetTests                 := Seq.empty,
      SK.ignoreSymbols               := Seq.empty,
      SK.activeMutators              := allMutators,
      SK.analyserJavaOptions         := (K.javaOptions in Test).value,
      SK.verbose                     := false,
      SK.recompileOnly               := false
    ) :+ dependencies

  def dependencies: Setting[Seq[ModuleID]] =
    K.libraryDependencies += (organization % s"${artifactId}_${K.scalaBinaryVersion.value}" % version % MutationTest)
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

  private def frameworkNames(maybeFramework: Option[TestFramework]): Seq[String] =
    maybeFramework match {
      case None                                                     => Seq("scalatest, specs2, utest, junit")
      case Some(framework) if framework == TestFrameworks.ScalaTest => Seq("scalatest")
      case Some(framework) if framework == TestFrameworks.JUnit     => Seq("junit")
      case Some(framework) if framework == TestFrameworks.Specs2    => Seq("specs2")
    }

  private def aggregateTask[T](
    taskKey: TaskKey[T],
    configurations: Configuration*
  ): Def.Initialize[Task[Seq[T]]] = Def.taskDyn {
    val projectFilter       = inAggregates(K.thisProjectRef.value)
    val configurationFilter = if (configurations.isEmpty) inAnyConfiguration else inConfigurations(configurations: _*)
    val filter              = ScopeFilter(projectFilter, configurationFilter)
    taskKey.all(filter)
  }

  private def aggregateSetting[T](
    settingKey: SettingKey[T],
    configurations: Configuration*
  ): Def.Initialize[Seq[T]] = Def.settingDyn {
    val projectFilter       = inAggregates(K.thisProjectRef.value)
    val configurationFilter = if (configurations.isEmpty) inAnyConfiguration else inConfigurations(configurations: _*)
    val filter              = ScopeFilter(projectFilter, configurationFilter)
    settingKey.all(filter)
  }

  private def aggregateClassPath(config: Configuration): Def.Initialize[Task[Set[File]]] =
    aggregateTask(K.fullClasspath, config).map(
      cps => cps.flatten.map(_.data)(collection.breakOut)
    )

  private val testClassPath: Def.Initialize[Task[Set[File]]]    = aggregateClassPath(Test)
  private val compileClassPath: Def.Initialize[Task[Set[File]]] = aggregateClassPath(Compile)
  private val sourceDirs: Def.Initialize[Seq[Seq[File]]]        = aggregateSetting(K.sourceDirectories, Compile)
  private val testClassDirs: Def.Initialize[Seq[File]]          = aggregateSetting(K.crossTarget)

  private def scalamuArguments: Def.Initialize[Task[Seq[String]]] = Def.task {
    val aggregatedTestClassPath = testClassPath.value
    val aggregatedClassPath     = compileClassPath.value
    val aggregatedSourceDirs    = sourceDirs.value.flatten.distinct
    val aggregatedTestDirs      = testClassDirs.value.distinct

    val target           = K.target.value / "mutation-analysis-report"
    val vmParameters     = SK.analyserJavaOptions.value
    val scalacParameters = K.scalacOptions.value
    val targetClasses    = SK.targetClasses.value
    val targetTests      = SK.targetTests.value
    val ignoreSymbols    = SK.ignoreSymbols.value
    val timeoutFactor    = SK.timeoutFactor.value
    val timeoutConst     = SK.timeoutConst.value
    val parallelism      = SK.parallelism.value
    val verbose          = SK.verbose.value
    val recompileOnly    = SK.recompileOnly.value
    val activeMutators   = SK.activeMutators.value
    val testOptions      = K.testOptions.value

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
      optionString(aggregatedClassPath.map(_.getAbsolutePath), ",", "cp"),
      optionString(aggregatedTestClassPath.map(_.getAbsolutePath), ",", "tcp"),
      optionString(vmParameters, " ", "vmParameters"),
      optionString(targetClasses.map(_.toString), ",", "targetClasses"),
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
      aggregatedSourceDirs.map(_.getAbsolutePath).mkString(","),
      aggregatedTestDirs.map(_.getAbsolutePath).mkString(",")
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

  lazy val mutationTestTask: Def.Initialize[Task[Unit]] = Def.task {
    val scalaVersion = K.scalaBinaryVersion.value
    val log          = K.streams.value.log

    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 11)) | Some((2, 12)) =>
        val cp          = Classpaths.managedJars(MutationTest, Set("jar"), K.update.value)
        val forkOptions = ForkOptions()
        val run         = new ForkRun(forkOptions)
        val arguments   = scalamuArguments.value
        val javaOptions = (K.javaOptions in SK.mutationTest).value

        val runResult = run.run(
          mainClass,
          cp.map(_.data),
          javaOptions ++ arguments,
          log
        )
        runResult.failed.foreach(e => sys.error(e.getMessage))

      case _ => log.error(s"Unsupported scala version $scalaVersion. Supported versions include scala 2.11 & 2.12.")
    }
  }
}
