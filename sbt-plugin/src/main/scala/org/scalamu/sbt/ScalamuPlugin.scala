package org.scalamu.sbt

import sbt.{Def, _}
import sbt.Keys._
import sbt.plugins.JvmPlugin

object ScalamuPlugin extends AutoPlugin {
  private val OrgScalamu = "org.scalamu"
  private val artifactId = "entry-point"
  private val version    = "0.1-SNAPSHOT"
  private val mainClass  = "org.scalamu.entry.EntryPoint"

  override def requires: Plugins                   = JvmPlugin
  override def trigger: PluginTrigger              = allRequirements
  override def globalSettings: Seq[Def.Setting[_]] = Seq(commands += mutationTest)

  private def buildScalamuArguments(state: State): Seq[String] = {
    val extracted = Project.extract(state)
    import extracted._
    val baseDir       = get[File](baseDirectory)
    val sourceDirs    = get[Seq[File]](sources)
    val target        = get[File](crossTarget)
    val classes       = target / "classes"
    val testClasses   = target / "testClasses"
    val testClassPath = get[Seq[Attributed[File]]](fullClasspath.in(Test))
    val (_, si) = runTask(scalaInstance, state)
    si.compilerJar
    state.configuration
    inConfig(Test)
    ???
  }

  lazy val mutationTest: Command = Command.command("mutationTest") { state =>
    val extracted = Project.extract(state)
    import extracted._
    val binaryVersion = get(scalaBinaryVersion)

    val newS = append(
      Seq(
        libraryDependencies += OrgScalamu % s"${artifactId}_$binaryVersion" % version % Test
      ),
      state
    )

    val (_, tryUpdate) = Project.runTask[UpdateReport](update, newS).get

    tryUpdate match {
      case Inc(cause) => state.log.error(s"Failed to resolve dependencies. Aborting.")
      case Value(report) =>
        val (_, si) = runTask(scalaInstance, state)
        val run     = new Run(si, false, get(taskTemporaryDirectory))
        val cp      = report.matching(artifactFilter(name = "*entry-point*", `type` = "jar"))
        run.run(
          mainClass,
          cp,
          buildScalamuArguments(state),
          state.log
        )
    }
    state
  }
}
