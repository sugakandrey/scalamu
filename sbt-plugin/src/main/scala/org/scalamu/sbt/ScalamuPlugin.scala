package org.scalamu.sbt

import sbt.{Def, _}
import sbt.{Keys => K}
import sbt.plugins.JvmPlugin
import org.scalamu.sbt.Import.{ScalamuKeys => SK}

object ScalamuPlugin extends AutoPlugin {
  private val organization = "io.github.sugakandrey"
  private val artifactId   = "scalamu-compilation"
  private val version      = "0.1-SNAPSHOT"
  private val mainClass    = "org.scalamu.entry.EntryPoint"

  val autoImport: Import.type = Import

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
      projects.foldLeft((Set.empty[File], Set.empty[File], Set.empty[File], Set.empty[File])) { (acc, ref) =>
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
    val (_, javaOptions)   = runTask(SK.analyserJavaOptions, state)
    val (_, scalacOptions) = runTask(K.scalacOptions, state)
    val excludeSource      = get(SK.includeSources)
    val excludeTests       = get(SK.includeTests)
    val timeoutFactor      = get(SK.timeoutFactor)
    val timeoutConst       = get(SK.timeoutConst)
    val parallelism        = get(SK.parallelism)
    val verbose            = get(SK.verbose)
    val recompileOnly      = get(SK.recompileOnly)
    val activeMutators     = get(SK.activeMutators)
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
      optionString(aggregatedClassPath.map(_.getAbsolutePath), ",", "cp"),
      optionString(aggregatedTestClassPath.map(_.getAbsolutePath), ",", "tcp"),
      optionString(javaOptions, " ", "vmParameters"),
      optionString(excludeSource.map(_.toString), ",", "targetSources"),
      optionString(excludeTests.map(_.toString), ",", "targetTests"),
      optionString(scalacOptions, " ", "scalacParameters")
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
    def isValidJar(candidate: File): Boolean =
      candidate.exists() && candidate.isFile && candidate.getName.endsWith(".jar")

    val extracted = Project.extract(state)

    val log           = state.log
    val binaryVersion = extracted.get(K.scalaBinaryVersion)
    val assemblyJar   = extracted.getOpt(SK.scalamuJarPath)

    assemblyJar match {
      case Some(jar) if isValidJar(jar) =>
        CrossVersion.partialVersion(binaryVersion) match {
          case Some((2, 11)) | Some((2, 12)) =>
            val guardArtifact = organization % s"${artifactId}_$binaryVersion" % version

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
              Seq(jar),
              arguments,
              log
            )

            state
          case _ =>
            log.error(s"Unsupported scala version: $binaryVersion. Only supported major version are 2.11 & 2.12.")
            state.fail
        }
      case _ =>
        log.warn(s"No usable scalamu jar found. Attempting to download scalamu.jar to ${}.")
        state.fail
    }
  }
}
