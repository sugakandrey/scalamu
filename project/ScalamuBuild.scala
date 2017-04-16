import sbt.Keys._
import sbt._

object ScalamuBuild {
  lazy val commonSettings = Seq(
    scalaVersion := "2.12.1",
    scalacOptions := Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-unused-import",
      "-Xfuture"
//      "-Xfatal-warnings"
    ),
    fork in Test := true,
    initialCommands in console := """
      import scala.reflect.runtime.universe._
      import scala.reflect.runtime.currentMirror
      import scala.tools.reflect.ToolBox
      val tb = currentMirror.mkToolBox() 
      """,
    scalacOptions in (Compile, console) -= "-Ywarn-unused-import"
  )

  def testDependencies(configs: Configuration*) = Seq(
    "org.scalactic"  %% "scalactic"  % "3.0.1"  % configs.mkString(","),
    "org.scalatest"  %% "scalatest"  % "3.0.1"  % configs.mkString(",")
  )

  lazy val testSettings = inConfig(Test)(
    fullClasspath ++= (fullClasspath in Compile).value
      .filter(_.data.getName.contains("org.scala-lang"))
  )

  lazy val plugin = Project(id = "plugin", base = file("plugin"))
    .settings(commonSettings)
    .settings(testSettings, Defaults.itSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
        "org.slf4j"      % "slf4j-api"      % "1.7.25"
      ) ++ testDependencies(Test)
    )

  lazy val commandLine = Project(id = "cli", base = file("cli"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.ow2.asm" % "asm-commons" % "5.2",
        "org.ow2.asm" % "asm-util"    % "5.2"
      ) ++ testDependencies(Test)
    )
    .dependsOn(plugin)

  lazy val root = Project(id = "scalamu", base = file("."))
    .settings(commonSettings)
    .aggregate(plugin, commandLine)
}

object ScalamuTestingBuild {
  import ScalamuBuild._
  lazy val allTestingFrameWorks = Seq(
    libraryDependencies ++= Seq(
      "org.specs2"    %% "specs2-core" % "3.8.9" % Test,
      "org.scalatest" %% "scalatest"   % "3.0.1" % Test,
      "com.lihaoyi"   %% "utest"       % "0.4.5" % Test,
      "junit"         % "junit"        % "4.12"  % Test
    )
  )

  private def testProject(name: String): Project =
    Project(id = s"testing-$name", base = file(s"testing/$name"))

  lazy val testingSimple = testProject("simple")
    .settings(commonSettings)
    .settings(allTestingFrameWorks)
}
