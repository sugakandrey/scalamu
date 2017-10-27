scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.specs2"    %% "specs2-core" % "4.0.0" % Test,
  "org.scalatest" %% "scalatest"   % "3.0.4" % Test,
  "com.lihaoyi"   %% "utest"       % "0.6.0" % Test,
  "junit"         % "junit"        % "4.12"  % Test
)

lazy val check = TaskKey[Unit]("check")

check := {
  val reportDir = file("target/mutation-analysis-report")
  if (!reportDir.exists()) sys.error("Report directory doesn't exist.")

  val barAnnSource = reportDir / "example" / "Bar.scala.html"
  val bazAnnSource = reportDir / "example" / "Baz.scala.html"
  val fooAnnSource = reportDir / "example" / "Foo.scala.html"

  Seq(barAnnSource, bazAnnSource, fooAnnSource).foreach { f =>
    if (!f.exists()) sys.error(s"Annotated source file: ${f.getName} doesn't exist.")
  }
}
