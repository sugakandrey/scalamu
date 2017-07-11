package org.scalamu.sbt

import sbt.{Def, _}
import sbt.Keys._
import sbt.plugins.JvmPlugin

object ScalamuPlugin extends AutoPlugin {
  private val OrgScalamu = "org.scalamu"
  private val artifactId = "entry-point"
  private val version    = "0.1-SNAPSHOT"
  private val mainClass  = "org.scalamu.entry.EntryPoint"

  val autoImport = ScalamuKeys
  import autoImport._

  override def requires: Plugins      = JvmPlugin
  override def trigger: PluginTrigger = allRequirements
  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    commands ++= Seq(mutationTest, mutationTestAggregated, nameCommand),
    scalamuTimeoutFactor := 1.5,
    scalamuParallelism := 1,
    scalamuTimeoutConst := 2000,
    scalamuExclludeSources := Seq.empty,
    scalamuExcludeTests := Seq.empty,
    scalamuVerbose := false,
    scalamuActiveMutations := allMutations
  )

  private def allMutations: Seq[String] = Seq(
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

  private def buildScalamuArguments(state: State): Seq[String] = {
    val extracted = Project.extract(state)
    import extracted._

    val sourceDirs         = get(sourceDirectory)
    val target             = get(Keys.target)
    val crossTarget        = get(Keys.crossTarget)
    val testClasses        = crossTarget / "testClasses"
    val (_, testClassPath) = runTask(fullClasspath in Test, state)
    val (_, javaOptions)   = runTask(Keys.javaOptions in Test, state)
    val (_, scalacOptions) = runTask(Keys.scalacOptions, state)
    val excludeSource      = get(scalamuExclludeSources)
    val excludeTests       = get(scalamuExcludeTests)
    val timeoutFactor      = get(scalamuTimeoutFactor)
    val timeoutConst       = get(scalamuTimeoutConst)
    val parallelism        = get(scalamuParallelism)
    val verbose            = get(scalamuVerbose)
    val activeMutations    = get(scalamuActiveMutations)
    val (_, testOptions)   = runTask(Keys.testOptions, state)

    val testRunnerArgs = testOptions
      .foldLeft(Map.empty[String, String]) {
        case (acc, Tests.Argument(framework, args)) =>
          val names = frameworkNames(framework)
          names.foldLeft(acc) {
            case (namesAcc, fname) =>
              val oldVal  = namesAcc.getOrElse(fname, "")
              val updated = s"$oldVal ${args.mkString(" ")}"
              namesAcc + (fname -> updated)
          }
        case (acc, _) => acc
      }
      .map {
        case (fname, args) => s"$fname=$args"
      }
      .mkString

    Seq(
      target.getAbsolutePath,
      sourceDirs.getAbsolutePath,
      testClasses.getAbsolutePath,
      s"--cp ${testClassPath.map(_.data.getAbsolutePath).mkString(",")}",
      s"--jvmOpts ${javaOptions.mkString(" ")}",
      s"--excludeSource ${excludeSource.map(_.toString).mkString(",")}",
      s"--excludeTestClasses ${excludeTests.map(_.toString()).mkString(",")}",
      s"--scalacOptions ${scalacOptions.mkString(" ")}",
      s"--timeoutFactor $timeoutFactor",
      s"--timeoutConst $timeoutConst",
      s"--parallelism $parallelism",
      s"--mutations ${activeMutations.mkString(" ")}"
    ) ++ (if (verbose) Seq("--verbose") else Seq.empty) ++
      (if (testRunnerArgs.nonEmpty) Seq(s"--testOptions $testRunnerArgs")
       else Seq.empty)
  }

  lazy val mutationTestAggregated: Command = Command.command("mutationTestAggregated") { state =>
    val extracted = Project.extract(state)
    import sbt.Project.showContextKey

    extracted.structure.allProjectRefs.foreach { ref =>
      val ext = Extracted(extracted.structure, extracted.session, ref)(showContextKey(state))
      ext.
      Command.process("nameCommand", state)
    }
    state
  }
  
  lazy val nameCommand: Command = Command.command("nameCommand") { state =>
    val extracted = Project.extract(state)
    import extracted._
    println(get(sourceDirectory))
    state
  }

  lazy val mutationTest: Command = Command.command("mutationTest") { state =>
    val extracted = Project.extract(state)
    import extracted._
    extracted.structure.allProjectRefs
    val binaryVersion = get(scalaBinaryVersion)
    val (compiled, _) = runTask(compile in Test, state)

    val newS = append(
      Seq(
        libraryDependencies += OrgScalamu % s"${artifactId}_$binaryVersion" % version classifier "assembly"
      ),
      compiled
    )

    val (_, tryUpdate) = Project.runTask[UpdateReport](update, newS).get

    tryUpdate match {
      case Inc(_) => state.log.error(s"Failed to resolve dependencies. Aborting.")
      case Value(report) =>
        val (_, si) = runTask(scalaInstance, state)
        val run     = new Run(si, trapExit = true, get(taskTemporaryDirectory))

        val cp = report.matching(
          artifactFilter(name = "*entry-point*", `type` = "jar", `classifier` = "assembly")
        )

        val arguments = buildScalamuArguments(state)
        run.run(
          mainClass,
          cp,
          arguments,
          state.log
        )
    }
    state
  }
}
