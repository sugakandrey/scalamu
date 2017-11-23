package org.scalamu.sbt

import org.scalamu.sbt.Mutators.Mutator
import sbt._

import scala.util.matching.Regex

trait ScalamuKeys {
  lazy val scalamuRun                 = taskKey[Unit]("Runs mutation analysis")
  lazy val scalamuTimeoutFactor       = settingKey[Double]("Test timeout factor.")
  lazy val scalamuTimeoutConst        = settingKey[Long]("Flat amount of additional run time for tests.")
  lazy val scalamuParallelism         = settingKey[Int]("Number of running analysers.")
  lazy val scalamuVerbose             = settingKey[Boolean]("Enables verbose logging.")
  lazy val scalamuTargetOwners        = settingKey[Seq[Regex]]("Only mutate trees with certain owner names.")
  lazy val scalamuTargetTests         = settingKey[Seq[Regex]]("Only run certain tests.")
  lazy val scalamuIgnoreSymbols       = settingKey[Seq[Regex]]("Ignore symbols with certain names.")
  lazy val scalamuEnabledMutators     = settingKey[Seq[Mutator]]("Set of enabled mutation operators.")
  lazy val scalamuRecompileOnly       = settingKey[Boolean]("Do not run any analysis.")
  lazy val scalamuAnalyserJavaOptions = taskKey[Seq[String]]("Options passed to analyser JVM.")
}

object ScalamuKeys extends ScalamuKeys
