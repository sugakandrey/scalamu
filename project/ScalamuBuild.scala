import com.typesafe.sbt.pgp.PgpKeys
import play.twirl.sbt.Import.TwirlKeys
import play.twirl.sbt.SbtTwirl
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.ShadeRule

object ScalamuBuild {
  val GPL3 = "GPL 3.0" -> url("http://www.gnu.org/licenses/gpl-3.0.en.html")

  val specs2    = "org.specs2"    %% "specs2-core" % "3.8.9"
  val scalatest = "org.scalatest" %% "scalatest"   % "3.0.1"
  val utest     = "com.lihaoyi"   %% "utest"       % "0.4.5"
  val junit     = "junit"         % "junit"        % "4.12"

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.8.0")

  lazy val commonSettings = Seq(
    test in assembly   := {},
    organization       := "io.github.sugakandrey",
    scalaVersion       := "2.12.2",
    crossScalaVersions := Seq("2.11.11", "2.12.2"),
    scalacOptions := Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
//      "-Ywarn-unused-import",
      "-Xfuture",
      "-Xexperimental"
//      "-Xfatal-warnings"
    ),
    fork in Test               := true,
    initialCommands in console := """
      import scala.reflect.runtime.universe._
      import scala.reflect.runtime.currentMirror
      import scala.tools.reflect.ToolBox
      val tb = currentMirror.mkToolBox() 
      """,
    scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      scalatest        % Test
    ) ++
      (if (scalaBinaryVersion.value == "2.10") Seq()
       else
         Seq(
           "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
         )),
    publishMavenStyle       := true,
    publishArtifact in Test := false,
    publishTo := {
      if (isSnapshot.value)
        Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      else
        Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }
  )

  lazy val common = Project(id = "common", base = file("common"))
    .settings(commonSettings)

  lazy val plugin = Project(id = "plugin", base = file("plugin"))
    .settings(commonSettings)
    .settings(libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value)
    .dependsOn(common)

  private lazy val testingFrameworks = Seq(scalatest, specs2, utest, junit)

  lazy val commandLine = Project(id = "cli", base = file("cli"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.ow2.asm"      % "asm-commons"                  % "5.2",
        "org.ow2.asm"      % "asm-util"                     % "5.2",
        "org.typelevel"    %% "cats"                        % "0.9.0",
        "com.github.scopt" %% "scopt"                       % "3.5.0",
        "org.scalamock"    %% "scalamock-scalatest-support" % "3.5.0" % Test,
        "com.ironcorelabs" %% "cats-scalatest"              % "2.2.0" % Test
      ) ++ testingFrameworks.map(_ % Optional) ++ circe
    )
    .dependsOn(plugin % "compile->compile;test->test")
    .dependsOn(common, compilation)

  lazy val report = Project(id = "report", base = file("report"))
    .settings(commonSettings)
    .enablePlugins(SbtTwirl)
    .settings(TwirlKeys.templateImports := Seq())
    .dependsOn(commandLine, common, plugin)

  lazy val root = Project(id = "scalamu", base = file("."))
    .aggregate(
      plugin,
      commandLine,
      report,
      common,
      entryPoint,
      compilation
    )

  lazy val entryPoint = Project(id = "entry-point", base = file("entry-point"))
    .settings(commonSettings)
    .dependsOn(commandLine, common, plugin, report, compilation)
    .settings(
      PgpKeys.useGpg := true,
      artifact in (Compile, assembly) ~= { _.copy(`classifier` = Some("assembly")) },
      addArtifact(artifact in (Compile, assembly), assembly),
      assemblyShadeRules in assembly :=
        Seq("io.circe.**", "org.ow2.asm.**", "org.typelevel.**", "shapeless.**").map(shade),
      isSnapshot := true,
      homepage   := Some(url("https://github.com/sugakandrey/scalamu")),
      licenses   := Seq(GPL3),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/sugakandrey/scalamu"),
          "git@github.com:sugakandrey/scalamu.git"
        )
      ),
      developers += Developer(
        id    = "sugakandrey",
        name  = "Andrey Sugak",
        email = "sugak.andr3y@gmail.com",
        url   = url("https://github.com/sugakandrey")
      ),
      pomIncludeRepository := Function.const(false),
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishArtifact in Test := false
    )

  private def shade(pattern: String): ShadeRule =
    ShadeRule.rename(pattern -> "shaded.@0").inAll

  lazy val compilation = Project(id = "compilation", base = file("compilation"))
    .settings(commonSettings)
    .settings(
      libraryDependencies := Seq(
        "org.scoverage" %% "scalac-scoverage-plugin"  % "1.3.0",
        "org.scoverage" %% "scalac-scoverage-runtime" % "1.3.0"
      )
    )

  lazy val pluginSettings = Seq(
    organization                   := "io.github.sugakandrey",
    sbtPlugin                      := true,
    name                           := "sbt-scalamu",
    CrossBuilding.crossSbtVersions := Seq("0.13.16", "1.0.0")
  )

  lazy val scalamuSbt = Project(id = "sbt-plugin", base = file("sbt-plugin"))
    .settings(pluginSettings)

  private def pluginProject(name: String, version: String): Project =
    Project(name, file(name))
      .settings(pluginSettings)
      .settings(
        libraryDependencies += "io.github.sugakandrey" % s"scalamu_$version" % "0.1-SNAPSHOT"
      )
      .settings(
        scalaSource in Compile := baseDirectory.value / ".." / ".." / "sbt-plugin" / "scr" / "main" / "scala",
        scalaSource in Test    := baseDirectory.value / ".." / ".." / "sbt-plugin" / "scr" / "main" / "scala"
      )

  lazy val scalamuSbt_211 = pluginProject("scalamu-sbt_2.11", "2.11")
  lazy val scalamuSbt_212 = pluginProject("scalamu-sbt_2.12", "2.12")
}

object ScalamuTestingBuild {
  import ScalamuBuild._

  private def testProject(name: String): Project =
    Project(id = s"testing-$name", base = file(s"testing/$name"))
      .settings(commonSettings)

  lazy val testingSimple = testProject("simple")
    .settings(testFrameworks += new TestFramework("utest.runner.Framework"))

  lazy val junit = testProject("junit")
    .settings(libraryDependencies += ScalamuBuild.junit % Test)

  lazy val scalatest = testProject("scalatest")
    .settings(libraryDependencies += ScalamuBuild.scalatest % Test)

  lazy val specs2 = testProject("specs2")
    .settings(libraryDependencies += ScalamuBuild.specs2 % Test)

  lazy val utest = testProject("utest")
    .settings(libraryDependencies += ScalamuBuild.utest % Test)

  lazy val withScoverage = testProject("scoverage")
    .settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.0.1" % Test,
        "junit"         % "junit"      % "4.12"  % Test
      )
    )
}
