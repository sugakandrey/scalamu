scalaVersion := "2.12.3"

lazy val check = TaskKey[Unit]("check")

check := {
  val ccp = (managedClasspath in Compile).value
  if (ccp.exists(_.data.getName.contains("scalamu")))
    sys.error("Compile classpath contains scalamu jar.")

  val tcp = (managedClasspath in Scalamu).value
  if (!tcp.exists(_.data.getName.contains("scalamu")))
    sys.error("Scalamu classpath doesn't contain scalamu jar.")
}
