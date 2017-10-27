import com.typesafe.sbt.pgp.PgpKeys
import bintray.BintrayPlugin
import bintray.BintrayKeys._
import com.typesafe.sbt.{GitPlugin, GitVersioning, SbtPgp}
import org.jetbrains.sbtidea.Keys._
import org.jetbrains.sbtidea.SbtIdeaPlugin
import play.twirl.sbt.Import.TwirlKeys
import play.twirl.sbt.SbtTwirl
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.ShadeRule
import scoverage.ScoverageSbtPlugin
import xerial.sbt.Sonatype

object ScalamuBuild {
  val GPL3 = "GPL 3.0" -> url("http://www.gnu.org/licenses/gpl-3.0.en.html")

  val specs2    = "org.specs2"    %% "specs2-core" % "4.0.0"
  val scalatest = "org.scalatest" %% "scalatest"   % "3.0.4"
  val utest     = "com.lihaoyi"   %% "utest"       % "0.6.0"
  val junit     = "junit"         % "junit"        % "4.12"

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.8.0")

  lazy val commonSettings = Seq(
    version            := "0.1.0-SNAPSHOT",
    isSnapshot         := true,
    test in assembly   := {},
    organization       := "io.github.sugakandrey",
    scalaVersion       := "2.12.4",
    crossScalaVersions := Seq("2.11.11", "2.12.4"),
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
    scalacOptions in (Compile, console) -= "-Ywarn-unused-import"
  ) ++ publishSettings

  private def createProject(id: String, base: File): Project =
    Project(id, base)
      .disablePlugins(ScriptedPlugin, BintrayPlugin, GitPlugin, ScoverageSbtPlugin)
      .settings(commonSettings)

  lazy val commonDeps = Seq(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      scalatest        % Test
    ) ++
      (if (scalaBinaryVersion.value == "2.10") Seq()
       else
         Seq(
           "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
         ))
  )

  lazy val publishSettings = Seq(
    PgpKeys.useGpg          := true,
    homepage                := Some(url("https://github.com/sugakandrey/scalamu")),
    licenses                := Seq(GPL3),
    publishArtifact in Test := false,
    publishMavenStyle       := true,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/sugakandrey/scalamu"),
        "git@github.com:sugakandrey/scalamu.git"
      )
    ),
    developers += Developer(
      id = "sugakandrey",
      name = "Andrey Sugak",
      email = "sugak.andr3y@gmail.com",
      url = url("https://github.com/sugakandrey")
    ),
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )

  lazy val common = createProject(id = "common", base = file("common"))
    .settings(commonDeps)
    .settings(name := "scalamu-common")

  lazy val scalacPlugin = createProject(id = "scalac-plugin", base = file("scalac-plugin"))
    .settings(commonDeps)
    .settings(
      name                := "scalamu-scalac",
      libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
    )
    .dependsOn(common)

  private lazy val testingFrameworks = Seq(scalatest, specs2, utest, junit)

  lazy val commandLine = createProject(id = "cli", base = file("cli"))
    .settings(commonDeps)
    .settings(
      name := "scalamu-cli",
      libraryDependencies ++= Seq(
        "org.ow2.asm"      % "asm-commons"                  % "5.2",
        "org.ow2.asm"      % "asm-util"                     % "5.2",
        "org.typelevel"    %% "cats"                        % "0.9.0",
        "com.github.scopt" %% "scopt"                       % "3.5.0",
        "org.scalamock"    %% "scalamock-scalatest-support" % "3.5.0" % Test,
        "com.ironcorelabs" %% "cats-scalatest"              % "2.2.0" % Test
      ) ++
        testingFrameworks.map(_ % Optional) ++ circe
    )
    .dependsOn(scalacPlugin % "compile->compile;test->test")
    .dependsOn(common, compilation)

  lazy val report = createProject(id = "report", base = file("report"))
    .settings(commonDeps)
    .enablePlugins(SbtTwirl)
    .settings(
      name                      := "scalamu-report",
      TwirlKeys.templateImports := Seq()
    )
    .dependsOn(commandLine, common, scalacPlugin)

  lazy val root = createProject(id = "scalamu", base = file("."))
    .dependsOn(
      entryPoint
    )
    .aggregate(
      scalacPlugin,
      commandLine,
      report,
      common,
      entryPoint,
      compilation
    )
    .settings(
      artifact in (Compile, assembly) ~= { _.withClassifier(Some("assembly")) },
      addArtifact(artifact in (Compile, assembly), assembly),
      assemblyShadeRules in assembly :=
        Seq(
          "io.circe.**",
          "org.ow2.asm.**",
          "org.typelevel.**",
          "shapeless.**",
          "org.slf4j.**"
        ).map(shade)
    )

  lazy val entryPoint = createProject(id = "entry-point", base = file("entry-point"))
    .settings(commonDeps)
    .settings(name := "scalamu-entry-point")
    .dependsOn(
      commandLine,
      common,
      scalacPlugin,
      report,
      compilation
    )

  private def shade(pattern: String): ShadeRule =
    ShadeRule.rename(pattern -> "shaded.@0").inAll

  lazy val compilation = createProject(id = "compilation", base = file("compilation"))
    .settings(name := "scalamu-compilation")
    .settings(
      libraryDependencies ++= Seq(
        "org.scoverage" %% "scalac-scoverage-plugin"  % "1.3.0",
        "org.scoverage" %% "scalac-scoverage-runtime" % "1.3.0"
      )
    )

  import ScriptedPlugin.autoImport._
  import com.typesafe.sbt.GitPlugin.autoImport._
  lazy val scalamuSbt = Project(id = "sbt-plugin", base = file("sbt-plugin"))
    .settings(
      scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
      scriptedBufferLog  := false
    )
    .settings(
      isSnapshot       := false,
      organization     := "io.github.sugakandrey",
      sbtPlugin        := true,
      name             := "sbt-scalamu",
      crossSbtVersions := Seq("0.13.16", "1.0.2")
    )
    .disablePlugins(Sonatype, SbtPgp)
    .enablePlugins(GitVersioning)
    .settings(
      licenses                 := Seq(GPL3),
      publishArtifact in Test  := false,
      publishMavenStyle        := false,
      bintrayRepository        := "sbt-plugins",
      bintrayOrganization      := None,
      git.baseVersion          := "0.2.0",
      git.uncommittedSignifier := None
    )

  lazy val scalamuIdea = createProject(id = "idea-plugin", base = file("idea-plugin"))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      scalaVersion                     := "2.11.11",
      ideaBuild                        := "172.3968.16",
      onLoad in Global                 ~= { _.andThen("idea-plugin/updateIdea" :: _) },
      assemblyOption in assembly       ~= { _.copy(includeScala = false) },
      assemblyExcludedJars in assembly ++= ideaFullJars.value,
      ideaExternalPlugins += IdeaPlugin
        .Zip("scala-plugin", url("https://download.plugins.jetbrains.com/1347/37646/scala-intellij-bin-2017.2.6.zip"))
    )
    .disablePlugins(ScriptedPlugin, BintrayPlugin)

  lazy val ideaRunner = Project(id = "idea-runner", base = file("idea-plugin/target"))
    .settings(
      scalaVersion                := "2.11.11",
      autoScalaLibrary            := false,
      unmanagedJars in Compile    := (ideaMainJars in scalamuIdea).value,
      unmanagedJars in Compile    += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar",
      fork in run                 := true,
      mainClass in (Compile, run) := Some("com.intellij.idea.Main"),
      javaOptions in run ++= Seq(
        "-Xmx2g",
        "-XX:ReservedCodeCacheSize=240m",
        "-XX:MaxPermSize=250m",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-ea",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005",
        s"-Didea.home=${(ideaBaseDirectory in scalamuIdea).value.getPath}",
        "-Didea.is.internal=true",
        "-Didea.debug.mode=true",
        "-Dapple.laf.useScreenMenuBar=true",
        s"-Dplugin.path=${(assemblyOutputPath in (scalamuIdea, assembly)).value}",
        "-Didea.ProcessCanceledException=disabled"
      )
    )
    .disablePlugins(ScriptedPlugin, BintrayPlugin)
}

object ScalamuTestingBuild {
  import ScalamuBuild._

  private def testProject(name: String): Project =
    Project(id = s"testing-$name", base = file(s"testing/$name"))
      .settings(commonSettings ++ commonDeps)
      .disablePlugins(ScriptedPlugin, BintrayPlugin, GitPlugin)

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
        ScalamuBuild.scalatest % Test,
        ScalamuBuild.junit     % Test
      )
    )
}
