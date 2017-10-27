scalaVersion := "2.12.3"

target in Scalamu := file("./scalamu")

libraryDependencies ++= Seq(
  "org.specs2"    %% "specs2-core" % "3.8.9" % Test,
  "org.scalatest" %% "scalatest"   % "3.0.1" % Test,
  "com.lihaoyi"   %% "utest"       % "0.4.5" % Test,
  "junit"         % "junit"        % "4.12"  % Test
)

lazy val check = TaskKey[Unit]("check")

check := {
  val defaultReportDir = file("target/mutation-analysis-report")
  if (defaultReportDir.exists()) sys.error("Default report directory must not exist.")
  
  val reportDir = file("./scalamu")
  if (!reportDir.exists()) sys.error("Report directory doesn't exist.")

  val barAnnSource = reportDir / "example" / "Bar.scala.html"
  val bazAnnSource = reportDir / "example" / "Baz.scala.html"
  val fooAnnSource = reportDir / "example" / "Foo.scala.html"

  Seq(barAnnSource, bazAnnSource, fooAnnSource).foreach { f =>
    if (!f.exists()) sys.error(s"Annotated source file: ${f.getName} doesn't exist.")
  }
}
