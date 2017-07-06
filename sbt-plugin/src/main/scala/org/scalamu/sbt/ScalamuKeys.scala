package org.scalamu.sbt

import sbt._

import scala.util.matching.Regex

object ScalamuKeys {
  lazy val scalamuTimeoutFactor   = settingKey[Double]("a factor to apply to normal test duration before considering being in an inf. loop")
  lazy val scalamuTimeoutConst    = settingKey[Long]("additional flat amount of allowed test run time")
  lazy val scalamuParallelism     = settingKey[Int]("number of simultaneously running analyser JVMs")
  lazy val scalamuVerbose         = settingKey[Boolean]("enables verbose logging")
  lazy val scalamuExclludeSources = settingKey[Seq[Regex]]("exclude certain source files from being mutated")
  lazy val scalamuExcludeTests    = settingKey[Seq[Regex]]("exclude certain test classes from being run")
  lazy val scalamuActiveMutations = settingKey[Seq[String]]("set of active mutation operators")
}
