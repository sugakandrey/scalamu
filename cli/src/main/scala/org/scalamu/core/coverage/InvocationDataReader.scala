package org.scalamu.core.coverage

import java.io.File
import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.Logger
import scoverage.IOUtils

import scala.collection.Set
import scala.util.control.NonFatal

class InvocationDataReader(dir: Path) {
  import InvocationDataReader._

  log.debug(s"Initialized InvocationDataReader in $dir.")

  def invokedStatements(): Set[Int] = {
    val measurementFiles = IOUtils.findMeasurementFiles(dir.toFile)
    val invocations      = IOUtils.invoked(measurementFiles)
    log.debug(s"Measurements data files discovered: ${measurementFiles.map(_.toPath.toString).mkString(",")}")
    clearData(measurementFiles)
    invocations
  }

  def clearData(): Unit = clearData(IOUtils.findMeasurementFiles(dir.toFile))

  def clearData(files: Seq[File]): Unit = files.foreach { file =>
    try {
      Files.newOutputStream(file.toPath) // truncate invocation data file
    } catch {
      case NonFatal(e) => log.error(s"Unable to clear invocation data file. Cause: $e")
    }
  }
}

object InvocationDataReader {
  private val log = Logger[InvocationDataReader]
}
