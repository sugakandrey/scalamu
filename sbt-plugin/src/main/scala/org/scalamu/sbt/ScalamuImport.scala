package org.scalamu.sbt

import sbt._

import scala.util.matching.Regex

trait ScalamuImport {
  object ScalamuKeys {
    lazy val mutationTest        = taskKey[Unit]("run mutation analysis")
    lazy val timeoutFactor       = settingKey[Double]("a factor to apply to normal test duration before considering being in an inf. loop")
    lazy val timeoutConst        = settingKey[Long]("additional flat amount of allowed test run time")
    lazy val parallelism         = settingKey[Int]("number of simultaneously running analyser JVMs")
    lazy val verbose             = settingKey[Boolean]("enables verbose logging")
    lazy val targetClasses       = settingKey[Seq[Regex]]("only include certain classes in the mutation process")
    lazy val targetTests         = settingKey[Seq[Regex]]("only run certain tests")
    lazy val ignoreSymbols       = settingKey[Seq[Regex]]("ignore symbols with certain names")
    lazy val activeMutators      = settingKey[Seq[String]]("set of active mutation operators")
    lazy val recompileOnly       = settingKey[Boolean]("do not run any analysis (for internal testing)")
    lazy val scalamuJarPath      = settingKey[File]("path to a jar file with scalamu internals")
    lazy val analyserJavaOptions = taskKey[Seq[String]]("options passed to mutation analysis process when forking")
  }
}
