package org.scalamu.core.coverage

import java.io.File
import java.nio.file.{Files, Path, StandardOpenOption}

import com.typesafe.scalalogging.Logger
import scoverage.IOUtils

import scala.collection.Set
import scala.util.control.NonFatal

class InvocationDataReader(dir: Path) {
  import InvocationDataReader._

  def invokedStatements(): Set[Int] = {
    val measurementFiles = IOUtils.findMeasurementFiles(dir.toFile)
    val invocations      = IOUtils.invoked(measurementFiles)
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
