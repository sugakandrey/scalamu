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
    K.commands += mutationTest,
    SK.timeoutFactor       := 1.5,
    SK.parallelism         := 1,
    SK.timeoutConst        := 2000,
    SK.includeSources      := Seq.empty,
    SK.includeTests        := Seq.empty,
    SK.activeMutators      := allMutations,
    SK.analyserJavaOptions := (K.javaOptions in Test).value,
    SK.verbose             := false,
    SK.recompileOnly       := false
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

  private def buildScalamuArguments(projects: Seq[ProjectRef], state: State): Seq[String] = {
    val extracted = Project.extract(state)
    import extracted._
    import sbt.Project.showContextKey

    val (aggregatedSourceDirs, aggregatedTestDirs, aggregatedClassPath, aggregatedTestClassPath) =
      projects.foldLeft((Set.empty[File], Set.empty[File], Set.empty[File], Set.empty[File])) {
        (acc, ref) =>
          val (accSource, accTest, accCp, accTcp) = acc

          val ext                   = Extracted(extracted.structure, extracted.session, ref)(showContextKey(state))
          val sourceDirs            = ext.get(K.sourceDirectories in Compile)
          val testDir               = ext.get(K.crossTarget) / "test-classes"
          val (_, compileClassPath) = ext.runTask(K.dependencyClasspath in Compile, state)
          val cp                    = compileClassPath.map(_.data)
          val (_, testClassPath)    = ext.runTask(K.fullClasspath in Test, state)
          val tcp                   = testClassPath.map(_.data)

          (accSource ++ sourceDirs, accTest + testDir, accCp ++ cp, accTcp ++ tcp)
      }

    val target             = get(K.target)
    val javaOptions        = get(SK.analyserJavaOptions)
    val (_, scalacOptions) = runTask(K.scalacOptions, state)
    val excludeSource      = get(SK.includeSources)
    val excludeTests       = get(SK.includeTests)
    val timeoutFactor      = get(SK.timeoutFactor)
    val timeoutConst       = get(SK.timeoutConst)
    val parallelism        = get(SK.parallelism)
    val verbose            = get(SK.verbose)
    val recompileOnly      = get(SK.recompileOnly)
    val activeMutations    = get(SK.activeMutators)
    val (_, testOptions)   = runTask(K.testOptions, state)

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
      optionString(aggregatedClassPath.map(_.getAbsolutePath), ",", "--cp"),
      optionString(aggregatedTestClassPath.map(_.getAbsolutePath), ",", "--tcp"),
      optionString(javaOptions, " ", "--jvmOpts"),
      optionString(excludeSource.map(_.toString), ",", "--includeSource"),
      optionString(excludeTests.map(_.toString), ",", "--includeTestClasses"),
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
    else Seq(name, options.mkString(separator))

  private def resolveAggregates(extracted: Extracted): Seq[ProjectRef] = {
    import extracted._

    def findAggregates(project: ProjectRef): List[ProjectRef] =
      project :: (structure.allProjects(project.build).find(_.id == project.project) match {
        case Some(resolved) => resolved.aggregate.toList.flatMap(findAggregates)
        case None           => Nil
      })

    (currentRef :: currentProject.aggregate.toList.flatMap(findAggregates)).distinct
  }

  lazy val mutationTest: Command = Command.command("mutationTest") { state =>
    val extracted = Project.extract(state)

    val log           = state.log
    val binaryVersion = extracted.get(K.scalaBinaryVersion)

    CrossVersion.partialVersion(binaryVersion) match {
      case Some((2, 11)) | Some((2, 12)) =>
        val mainArtifact    = OrgScalamu % s"${entryPointId}_$binaryVersion" % version
        val guardArtifact   = OrgScalamu % s"${compilationModuleId}_$binaryVersion" % version
        val withAssemblyJar = extracted.append(Seq(K.libraryDependencies += mainArtifact), state)

        val assemblyExtracted = Project.extract(withAssemblyJar)
        val (_, report)       = assemblyExtracted.runTask(K.update, withAssemblyJar)

        val assemblyJar = report.matching(
          moduleFilter(organization = OrgScalamu) &&
            artifactFilter(name = "*entry-point*", `type` = "jar", classifier = "assembly")
        )

        val withGuardJar   = extracted.append(Seq(K.libraryDependencies += guardArtifact), state)
        val guardExtracted = Project.extract(withGuardJar)
        import guardExtracted._

        val (updated, _) = runTask(K.update, withGuardJar)
        runTask(K.compile in Test, updated)

        val forkOptions = ForkOptions()
        val run         = new ForkRun(forkOptions)
        val arguments   = buildScalamuArguments(resolveAggregates(extracted), updated)

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
}
