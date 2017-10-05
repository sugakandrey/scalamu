package org.scalamu.sbt

import java.io.File

import sbt.ScalaRun
import sbt.Logger

trait SbtBackCompat {
  def runAndPropagateResult(
    run: ScalaRun,
    mainClass: String,
    classpath: Seq[File],
    options: Seq[String],
    log: Logger
  ): Unit = {
    val runResult = run.run(mainClass, classpath, options, log)
    runResult.foreach(msg => throw new RuntimeException(msg))
  }
}
