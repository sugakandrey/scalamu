import sbt.Keys._
import sbt.{IntegrationTest => It, _}

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
    "org.scalactic" %% "scalactic" % "3.0.1" % configs.mkString(",") intransitive(),
    "org.scalatest" %% "scalatest" % "3.0.1" % configs.mkString(",") intransitive()
  )

  lazy val testSettings = inConfig(Test)(
    fullClasspath ++= (fullClasspath in Compile).value
      .filter(_.data.getName.contains("org.scala-lang"))
  )

  lazy val plugin = Project(id = "scalamu-plugin", base = file("scalamu-plugin"))
    .settings(commonSettings)
    .configs(It)
    .settings(testSettings, Defaults.itSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
        "org.slf4j"      % "slf4j-api"      % "1.7.25"
      ) ++ testDependencies(Test, It)
    )

  lazy val commandLine = Project(id = "scalamu-command-line", base = file("scalamu-command-line"))
    .settings(commonSettings)
    .dependsOn(plugin)

  lazy val root = Project(id = "scalamu", base = file("."))
    .settings(commonSettings)
    .aggregate(plugin, commandLine)
}
