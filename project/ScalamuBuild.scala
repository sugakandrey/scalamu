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
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-unused-import",
      "-Xfuture"
//      "-Xfatal-warnings"
    ),
    fork in Test := true
  )

  lazy val plugin = Project(id = "scalamu-plugin", base = file("scalamu-plugin"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
        "org.scalactic"  %% "scalactic"     % "3.0.1" % Test intransitive(),
        "org.scalatest"  %% "scalatest"     % "3.0.1" % Test intransitive(),
        "org.slf4j"      % "slf4j-api"      % "1.7.25"
      ),
      fullClasspath in Test ++= (fullClasspath in Compile).value
        .filter(_.data.getName.contains("org.scala-lang"))
    )

  lazy val commandLine = Project(id = "scalamu-command-line", base = file("scalamu-command-line"))
    .settings(commonSettings)
    .dependsOn(plugin)

  lazy val root = Project(id = "scalamu", base = file("."))
    .settings(commonSettings)
    .aggregate(plugin, commandLine)
}
