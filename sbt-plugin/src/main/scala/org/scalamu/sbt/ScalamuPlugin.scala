package org.scalamu.sbt

import sbt.{Def, _}
import sbt.{Keys => K}
import sbt.plugins.JvmPlugin
import org.scalamu.sbt.Import.{ScalamuKeys => SK}

object ScalamuPlugin extends AutoPlugin {
  private val OrgScalamu          = "org.scalamu"
  private val entryPointId        = "entry-point"
  private val compilationModuleId = "compilation"
  private val version             = "0.1-SNAPSHOT"
  private val mainClass           = "org.scalamu.entry.EntryPoint"

  val autoImport = Import

  override def requires: Plugins      = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    K.commands ++= Seq(mutationTest, mutationTestAggregated),
    SK.timeoutFactor   := 1.5,
    SK.parallelism     := 1,
    SK.timeoutConst    := 2000,
    SK.excludeSources  := Seq.empty,
    SK.excludeTests    := Seq.empty,
    SK.activeMutations := allMutations,
    SK.verbose         := false,
    SK.recompileOnly   := true
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

    val sourceDirs            = get(K.sourceDirectories in Compile)
    val target                = get(K.target)
    val crossTarget           = get(K.crossTarget)
    val testClasses           = crossTarget / "test-classes"
    val (_, compileClassPath) = runTask(K.dependencyClasspath in Compile, state)
    val (_, testClassPath)    = runTask(K.dependencyClasspath in Test, state)
    val (_, javaOptions)      = runTask(K.javaOptions in Test, state)
    val (_, scalacOptions)    = runTask(K.scalacOptions, state)
    val excludeSource         = get(SK.excludeSources)
    val excludeTests          = get(SK.excludeTests)
    val timeoutFactor         = get(SK.timeoutFactor)
    val timeoutConst          = get(SK.timeoutConst)
    val parallelism           = get(SK.parallelism)
    val verbose               = get(SK.verbose)
    val recompileOnly         = get(SK.recompileOnly)
    val activeMutations       = get(SK.activeMutations)
    val (_, testOptions)      = runTask(K.testOptions, state)

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
      .map { case (fname, args) => s"$fname=$args" }
      .mkString

    val possiblyUndefinedOptions = Seq(
      optionString(compileClassPath.map(_.data.getAbsolutePath), ",", "--cp"),
      optionString(javaOptions, " ", "--jvmOpts"),
      optionString(excludeSource.map(_.toString), ",", "--excludeSource"),
      optionString(excludeTests.map(_.toString), ",", "--excludeTestClasses"),
      optionString(scalacOptions, " ", "--scalacOptions")
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
    ) ++
      (if (verbose) Seq("--verbose") else Seq.empty) ++
      (if (testRunnerArgs.nonEmpty) Seq("--testOptions", testRunnerArgs) else Seq.empty) ++
//      (if (recompileOnly) Seq("--recompileOnly") else Seq.empty)
      Seq("--recompileOnly")

    val arguments = Seq(
      target.getAbsolutePath,
      sourceDirs.map(_.getAbsolutePath).mkString(","),
      testClasses.getAbsolutePath
    )

    possiblyUndefinedOptions ++ options ++ arguments
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
    val binaryVersion = initialExt.get(K.scalaBinaryVersion)

    CrossVersion.partialVersion(binaryVersion) match {
      case Some((2, 11)) | Some((2, 12)) =>
        val mainArtifact    = OrgScalamu % s"${entryPointId}_$binaryVersion" % version
        val guardArtifact   = OrgScalamu % s"${compilationModuleId}_$binaryVersion" % version
        val withAssemblyJar = initialExt.append(Seq(K.libraryDependencies += mainArtifact), state)

        val assemblyExtracted = Project.extract(withAssemblyJar)
        val (_, report)       = assemblyExtracted.runTask(K.update, withAssemblyJar)

        val assemblyJar = report.matching(
          moduleFilter(organization = OrgScalamu) &&
            artifactFilter(name = "*entry-point*", `type` = "jar", classifier = "assembly")
        )

        val withGuardJar   = initialExt.append(Seq(K.libraryDependencies += guardArtifact), state)
        val guardExtracted = Project.extract(withAssemblyJar)
        import guardExtracted._

        val (updated, _) = runTask(K.update, withGuardJar)
        runTask(K.compile in Test, updated)
        val forkOptions = ForkOptions()
        val run         = new ForkRun(forkOptions)
        val arguments   = buildScalamuArguments(withGuardJar)

        run.run(
          mainClass,
          assemblyJar,
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
