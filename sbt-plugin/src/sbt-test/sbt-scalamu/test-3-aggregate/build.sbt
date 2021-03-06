scalaVersion := "2.12.3"

lazy val baz = Project(id = "baz", base = file("baz")).settings(
  libraryDependencies += "org.specs2" %% "specs2-core" % "4.0.0" % Test
)

lazy val foo = Project(id = "foo", base = file("foo"))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test
  )
  .dependsOn(baz)

lazy val bar = Project(id = "bar", base = file("bar")).settings(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "utest" % "0.6.0" % Test,
    "junit"       % "junit"  % "4.12"  % Test
  )
)

lazy val root = Project(id = "root", base = file(".")).dependsOn(foo, bar)

lazy val check = TaskKey[Unit]("check")
lazy val checkNoAggregation = TaskKey[Unit]("checkNoAggregation")

check := {
  val reportDir = file("target/mutation-analysis-report")
  if (!reportDir.exists()) sys.error("Report directory doesn't exist.")

  val fooDir = reportDir / "example/foo"
  val barDir = reportDir / "example/bar"
  val bazDir = reportDir / "example/baz"

  Seq(fooDir, barDir, bazDir).foreach { f =>
    if (!f.isDirectory) sys.error(s"Package directory: ${f.getName} doesn't exist.")
  }

  val fooAnnSource = fooDir / "Foo.scala.html"
  val barAnnSource = barDir / "Bar.scala.html"
  val bazAnnSource = bazDir / "Baz.scala.html"
  Seq(fooAnnSource, barAnnSource, bazAnnSource).foreach { f =>
    if (!f.exists()) sys.error(s"Annotated source file: ${f.getName} doesn't exist.")
  }
}

checkNoAggregation := {
  val reportDir = file("foo/target/mutation-analysis-report")
  if (!reportDir.exists()) sys.error("Report directory doesn't exist.")

  val fooDir = reportDir / "example/foo"
  val bazDir = reportDir / "example/baz"
  
  if (!fooDir.exists()) sys.error(s""""Report directory $fooDir for package "foo" does not exist.""")
  if (bazDir.exists()) sys.error(s"Directory $bazDir should not be created, when aggregation is disabled.")
}
