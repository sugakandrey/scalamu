lazy val root = ScalamuBuild.root

/* Main modules */
lazy val plugin      = ScalamuBuild.plugin
lazy val commandLine = ScalamuBuild.commandLine
lazy val common      = ScalamuBuild.common
lazy val report      = ScalamuBuild.report
lazy val compilation = ScalamuBuild.compilation
lazy val entryPoint  = ScalamuBuild.entryPoint
lazy val scalamuSbt  = ScalamuBuild.scalamuSbt
lazy val scalamuIdea = ScalamuBuild.scalamuIdea
lazy val ideaRunner  = ScalamuBuild.runner

/* Testing subprojects */
lazy val testingSimple = ScalamuTestingBuild.testingSimple
lazy val junit         = ScalamuTestingBuild.junit
lazy val scalatest     = ScalamuTestingBuild.scalatest
lazy val specs2        = ScalamuTestingBuild.specs2
lazy val utest         = ScalamuTestingBuild.utest
lazy val scoverage     = ScalamuTestingBuild.withScoverage
