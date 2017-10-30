scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.specs2"    %% "specs2-core" % "4.0.0" % Test,
  "org.scalatest" %% "scalatest"   % "3.0.4" % Test,
  "com.lihaoyi"   %% "utest"       % "0.6.0" % Test,
  "junit"         % "junit"        % "4.12"  % Test
)

ScalamuKeys.targetOwners := Seq("example\\.Foo.*".r, "example\\.Bar.*".r)

testOptions ++= Seq(
  Tests.Argument(TestFrameworks.ScalaTest, "-l", "tags.Ignored"),
  Tests.Argument(TestFrameworks.Specs2, "exclude", "Ignored")
)

lazy val check = TaskKey[Unit]("check")

check := {
  val reportDir = file("target/mutation-analysis-report")
  if (!reportDir.exists()) sys.error("Report directory doesn't exist.")

  val barAnnSource = reportDir / "example" / "Bar.scala.html"
  val bazAnnSource = reportDir / "example" / "Baz.scala.html"
  val fooAnnSource = reportDir / "example" / "Foo.scala.html"

  Seq(barAnnSource, fooAnnSource).foreach { f =>
    if (!f.exists()) sys.error(s"Annotated source file: ${f.getName} doesn't exist.")
  }
  
  if (bazAnnSource.exists()) sys.error("Must not generate reports for files without mutations.")
}
