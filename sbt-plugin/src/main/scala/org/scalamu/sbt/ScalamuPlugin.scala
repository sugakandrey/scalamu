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
    commands ++= Seq(mutationTest, mutationTestAggregated),
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
    val testClasses        = crossTarget / "test-classes"
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

    val possiblyEmptyOptions = Seq(
      optionString(testClassPath.map(_.data.getAbsolutePath), ",", "--cp"),
      optionString(javaOptions, " ", "--jvmOpts"),
      optionString(excludeSource.map(_.toString), ",", "--excludeSource"),
      optionString(excludeTests.map(_.toString), ",", "--excludeTestClasses"),
      optionString(scalacOptions, "", "--scalacOptions")
    ).flatten

    val options = Seq(
      "--timeoutFactor",
      timeoutFactor.toString,
      "--timeoutConst",
      timeoutConst.toString,
      "--parallelism",
      parallelism.toString,
      "--mutations",
      activeMutations.mkString(",")
    ) ++ (if (verbose) Seq("--verbose") else Seq.empty) ++
      (if (testRunnerArgs.nonEmpty) Seq("--testOptions", testRunnerArgs)
       else Seq.empty)

    val arguments = Seq(
      target.getAbsolutePath,
      sourceDirs.getAbsolutePath,
      testClasses.getAbsolutePath
    )

    possiblyEmptyOptions ++ options ++ arguments
  }

  private def optionString[T](
    options: Seq[T],
    separator: String,
    name: String
  ): Seq[String] =
    if (options.isEmpty) Seq.empty
    else Seq(name, options.mkString(separator))

  private def mutationTestImpl(initialExt: Extracted, state: State): State = {
    val log           = state.log
    val binaryVersion = initialExt.get(scalaBinaryVersion)

    CrossVersion.partialVersion(binaryVersion) match {
      case Some((2, 11)) | Some((2, 12)) =>
        val arguments = buildScalamuArguments(state)

        val newS = initialExt.append(
          Seq(
            libraryDependencies += OrgScalamu % s"${artifactId}_$binaryVersion" % version classifier "assembly"
          ),
          state
        )

        val extracted = Project.extract(newS)
        import extracted._

        val (compiled, _) = runTask(compile in Test, newS)
        val (_, report)   = runTask(update, newS)
        val (_, si)       = runTask(scalaInstance, compiled)
        val forkOptions   = ForkOptions()
        val run           = new ForkRun(forkOptions)

        val cp = report.matching(
          artifactFilter(name = "*entry-point*", `type` = "jar", `classifier` = "assembly")
        )

        run.run(
          mainClass,
          cp,
          arguments,
          log
        )

        state
      case _ =>
        log.error(s"Unsupported scala version: $binaryVersion.")
        state.fail
    }
  }

  lazy val mutationTestAggregated: Command = Command.command("mutationTestAggregated") { state =>
    val extracted = Project.extract(state)
    import sbt.Project.showContextKey

    extracted.structure.allProjectRefs.foreach { ref =>
      val ext = Extracted(extracted.structure, extracted.session, ref)(showContextKey(state))
      mutationTestImpl(ext, state)
    }
    state
  }

  lazy val mutationTest: Command = Command.command("mutationTest") { state =>
    val extracted = Project.extract(state)
    mutationTestImpl(extracted, state)
  }
}
