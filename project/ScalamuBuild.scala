import com.typesafe.sbt.pgp.PgpKeys
import org.jetbrains.sbtidea.Keys._
import org.jetbrains.sbtidea.SbtIdeaPlugin
import play.twirl.sbt.Import.TwirlKeys
import play.twirl.sbt.SbtTwirl
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.ShadeRule

object ScalamuBuild {
  val GPL3 = "GPL 3.0" -> url("http://www.gnu.org/licenses/gpl-3.0.en.html")

  val specs2 = "org.specs2"       %% "specs2-core" % "3.8.9"
  val scalatest = "org.scalatest" %% "scalatest"   % "3.0.1"
  val utest = "com.lihaoyi"       %% "utest"       % "0.4.5"
  val junit = "junit"             % "junit"        % "4.12"

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.8.0")

  lazy val commonSettings = Seq(
    isSnapshot         := true,
    test in assembly   := {},
    organization       := "io.github.sugakandrey",
    scalaVersion       := "2.12.3",
    crossScalaVersions := Seq("2.11.11", "2.12.3"),
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
  )

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

  lazy val common = Project(id = "common", base = file("common"))
    .settings(commonSettings ++ commonDeps)
    .settings(name := "scalamu-common")
    .disablePlugins(ScriptedPlugin)

  lazy val scalacPlugin = Project(id = "scalac-plugin", base = file("scalac-plugin"))
    .settings(commonSettings ++ commonDeps)
    .settings(
      name                := "scalamu-scalac",
      libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
    )
    .dependsOn(common)
    .disablePlugins(ScriptedPlugin)

  private lazy val testingFrameworks = Seq(scalatest, specs2, utest, junit)

  lazy val commandLine = Project(id = "cli", base = file("cli"))
    .settings(commonSettings ++ commonDeps)
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
    .disablePlugins(ScriptedPlugin)

  lazy val report = Project(id = "report", base = file("report"))
    .settings(commonSettings ++ commonDeps)
    .enablePlugins(SbtTwirl)
    .settings(
      name                      := "scalamu-report",
      TwirlKeys.templateImports := Seq()
    )
    .dependsOn(commandLine, common, scalacPlugin)
    .disablePlugins(ScriptedPlugin)

  lazy val root = Project(id = "scalamu", base = file("."))
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
    .settings(commonSettings)
    .settings(publishSettings)
    .disablePlugins(ScriptedPlugin)

  lazy val scalamuAssembly = Project(id = "scalamu-assembly", base = file("target"))
    .settings(commonSettings)
    .settings(publishSettings)
    .dependsOn(root)
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
    .disablePlugins(ScriptedPlugin)

  lazy val entryPoint = Project(id = "entry-point", base = file("entry-point"))
    .settings(commonSettings ++ commonDeps)
    .settings(name := "scalamu-entry-point")
    .dependsOn(
      commandLine,
      common,
      scalacPlugin,
      report,
      compilation
    )
    .disablePlugins(ScriptedPlugin)

  private def shade(pattern: String): ShadeRule =
    ShadeRule.rename(pattern -> "shaded.@0").inAll

  lazy val compilation = Project(id = "compilation", base = file("compilation"))
    .settings(commonSettings)
    .settings(publishSettings)
    .settings(name := "scalamu-compilation")
    .settings(
      libraryDependencies ++= Seq(
        "org.scoverage" %% "scalac-scoverage-plugin"  % "1.3.0",
        "org.scoverage" %% "scalac-scoverage-runtime" % "1.3.0"
      )
    )
    .disablePlugins(ScriptedPlugin)

  import ScriptedPlugin.autoImport._
  lazy val scalamuSbt = Project(id = "sbt-plugin", base = file("sbt-plugin"))
    .settings(
      scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
      scriptedBufferLog  := false
    )
    .settings(
      organization     := "io.github.sugakandrey",
      sbtPlugin        := true,
      name             := "sbt-scalamu",
      crossSbtVersions := Seq("0.13.16", "1.0.0")
    )

  lazy val scalamuIdea = Project(id = "idea-plugin", base = file("idea-plugin"))
    .settings(commonSettings)
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
    .disablePlugins(ScriptedPlugin)

  lazy val ideaRunner = Project(id = "idea-runner", base = file("idea-plugin/target"))
    .dependsOn(scalamuIdea)
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
        "-Didea.is.internal=true",
        "-Didea.debug.mode=true",
        "-Dapple.laf.useScreenMenuBar=true",
        s"-Dplugin.path=${(assemblyOutputPath in (scalamuIdea, assembly)).value}",
        "-Didea.ProcessCanceledException=disabled"
      )
    )
    .disablePlugins(ScriptedPlugin)
}

object ScalamuTestingBuild {
  import ScalamuBuild._

  private def testProject(name: String): Project =
    Project(id = s"testing-$name", base = file(s"testing/$name"))
      .settings(commonSettings ++ commonDeps)

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
