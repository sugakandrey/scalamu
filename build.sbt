lazy val root = ScalamuBuild.root

/* Main modules */
lazy val plugin      = ScalamuBuild.plugin
lazy val commandLine = ScalamuBuild.commandLine

/* Testing subprojects */
lazy val testingSimple = ScalamuTestingBuild.testingSimple
lazy val junit         = ScalamuTestingBuild.junit
lazy val scalatest     = ScalamuTestingBuild.scalatest
lazy val specs2        = ScalamuTestingBuild.specs2
lazy val utest         = ScalamuTestingBuild.utest
lazy val scoverage     = ScalamuTestingBuild.withScoverage
