cancelable in ThisBuild := true

lazy val root = ScalamuBuild.root

/* Main modules */
lazy val plugin      = ScalamuBuild.scalacPlugin
lazy val core        = ScalamuBuild.core
lazy val common      = ScalamuBuild.common
lazy val report      = ScalamuBuild.report
lazy val compilation = ScalamuBuild.compilation
lazy val cli         = ScalamuBuild.cli
lazy val scalamuSbt  = ScalamuBuild.scalamuSbt
lazy val scalamuIdea = ScalamuBuild.scalamuIdea

/* Auxilary projects */
lazy val ideaRunner = ScalamuBuild.ideaRunner

/* Testing subprojects */
lazy val testingSimple = ScalamuTestingBuild.testingSimple
lazy val junit         = ScalamuTestingBuild.junit
lazy val scalatest     = ScalamuTestingBuild.scalatest
lazy val specs2        = ScalamuTestingBuild.specs2
lazy val utest         = ScalamuTestingBuild.utest
lazy val scoverage     = ScalamuTestingBuild.withScoverage

addCommandAlias("runIdea", "; idea-plugin/assembly; idea-runner/run")
addCommandAlias("publishAssembly", "scalamu/publishLocal")
