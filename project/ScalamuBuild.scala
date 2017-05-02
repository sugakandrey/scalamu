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
      "-language:higherKinds",
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
    scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
    libraryDependencies ++= Seq(
      "ch.qos.logback"             % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging"  % "3.5.0",
      "org.scalatest"              %% "scalatest"      % "3.0.1" % Test
    )
  )

  lazy val testSettings = inConfig(Test)(
    fullClasspath ++= (fullClasspath in Compile).value
      .filter(_.data.getName.contains("org.scala-lang"))
  )
  
  lazy val common = Project(id = "common", base = file("common"))
    .settings(commonSettings)

  lazy val plugin = Project(id = "plugin", base = file("plugin"))
    .settings(commonSettings)
    .settings(testSettings)
    .settings(
      libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
    )
  .dependsOn(common)  
  
  private lazy val testingFrameworks = Seq(
    "org.specs2"    %% "specs2-core" % "3.8.9" % Optional,
    "org.scalatest" %% "scalatest"   % "3.0.1" % Optional,
    "com.lihaoyi"   %% "utest"       % "0.4.5" % Optional,
    "junit"         % "junit"        % "4.12"  % Optional
  )

  lazy val commandLine = Project(id = "cli", base = file("cli"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.ow2.asm"      % "asm-commons"                  % "5.2",
        "org.ow2.asm"      % "asm-util"                     % "5.2",
        "org.typelevel"    %% "cats"                        % "0.9.0",
        "com.github.scopt" %% "scopt"                       % "3.5.0",
        "org.scoverage"    %% "scalac-scoverage-plugin"     % "1.3.0",
        "org.scoverage"    %% "scalac-scoverage-runtime"    % "1.3.0",
        
        "org.scalamock"    %% "scalamock-scalatest-support" % "3.5.0" % Test,
        "com.ironcorelabs" %% "cats-scalatest"              % "2.2.0" % Test
      ) ++ testingFrameworks
    )
    .settings(
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % "0.7.0")
    )    
    .dependsOn(plugin % "compile->compile;test->test")
    .dependsOn(common)  

  lazy val root = Project(id = "scalamu", base = file("."))
    .settings(commonSettings)
    .aggregate(plugin, commandLine)
}

object ScalamuTestingBuild {
  import ScalamuBuild._

  private def testProject(name: String): Project =
    Project(id = s"testing-$name", base = file(s"testing/$name"))
      .settings(commonSettings)

  lazy val testingSimple = testProject("simple")
    .settings(testFrameworks += new TestFramework("utest.runner.Framework"))

  lazy val junit = testProject("junit")
    .settings(libraryDependencies += "junit" % "junit" % "4.12" % Test)

  lazy val scalatest = testProject("scalatest")
    .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test)

  lazy val specs2 = testProject("specs2")
    .settings(libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.9" % Test)

  lazy val utest = testProject("utest")
    .settings(libraryDependencies += "com.lihaoyi" %% "utest" % "0.4.5" % Test)

  lazy val withScoverage = testProject("scoverage")
    .settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.0.1" % Test,
        "junit"         % "junit"      % "4.12"  % Test
      )
    )
}
