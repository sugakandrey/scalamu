scalaVersion := "2.12.3"

lazy val check = TaskKey[Unit]("check")

check := {
  val ccp = (fullClasspath in Compile).value
  if (ccp.exists(_.data.getName.contains("scalamu")))
    sys.error("Compile classpath contains scalamu jar.")

  val tcp = (fullClasspath in Scalamu).value
  if (!tcp.exists(_.data.getName.contains("scalamu-assembly")))
    sys.error("MutationTest classpath doesn't contain scalamu jar.")
}
