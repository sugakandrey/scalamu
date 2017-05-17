lazy val root = ScalamuBuild.root

/* Main modules */
lazy val plugin      = ScalamuBuild.plugin
lazy val commandLine = ScalamuBuild.commandLine
lazy val common      = ScalamuBuild.common
lazy val report      = ScalamuBuild.report
lazy val entryPoint  = ScalamuBuild.entryPoint

/* Testing subprojects */
lazy val testingSimple = ScalamuTestingBuild.testingSimple
lazy val junit         = ScalamuTestingBuild.junit
lazy val scalatest     = ScalamuTestingBuild.scalatest
lazy val specs2        = ScalamuTestingBuild.specs2
lazy val utest         = ScalamuTestingBuild.utest
lazy val scoverage     = ScalamuTestingBuild.withScoverage
