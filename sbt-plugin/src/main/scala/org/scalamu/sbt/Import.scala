package org.scalamu.sbt

import sbt._

import scala.util.matching.Regex

object Import {
  object ScalamuKeys {
    lazy val timeoutFactor   = settingKey[Double]("a factor to apply to normal test duration before considering being in an inf. loop")
    lazy val timeoutConst    = settingKey[Long]("additional flat amount of allowed test run time")
    lazy val parallelism     = settingKey[Int]("number of simultaneously running analyser JVMs")
    lazy val verbose         = settingKey[Boolean]("enables verbose logging")
    lazy val excludeSources  = settingKey[Seq[Regex]]("exclude certain source files from being mutated")
    lazy val excludeTests    = settingKey[Seq[Regex]]("exclude certain test classes from being run")
    lazy val activeMutations = settingKey[Seq[String]]("set of active mutation operators")
    lazy val recompileOnly   = settingKey[Boolean]("do not run any analysis (for internal testing)")
  }
}
