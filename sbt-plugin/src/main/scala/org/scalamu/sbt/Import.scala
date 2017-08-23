package org.scalamu.sbt

import sbt._

import scala.util.matching.Regex

object Import {
  object ScalamuKeys {
    lazy val timeoutFactor       = settingKey[Double]("a factor to apply to normal test duration before considering being in an inf. loop")
    lazy val timeoutConst        = settingKey[Long]("additional flat amount of allowed test run time")
    lazy val parallelism         = settingKey[Int]("number of simultaneously running analyser JVMs")
    lazy val verbose             = settingKey[Boolean]("enables verbose logging")
    lazy val includeSources      = settingKey[Seq[Regex]]("only include certain source files in the mutation process")
    lazy val includeTests        = settingKey[Seq[Regex]]("only run certain tests")
    lazy val activeMutators      = settingKey[Seq[String]]("set of active mutation operators")
    lazy val recompileOnly       = settingKey[Boolean]("do not run any analysis (for internal testing)")
    lazy val analyserJavaOptions = settingKey[Seq[String]]("options passed to mutation analysis process when forking")
  }
}
