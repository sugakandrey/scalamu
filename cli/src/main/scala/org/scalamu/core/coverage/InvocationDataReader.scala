package org.scalamu.core.coverage

import java.io.File
import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.Logger
import org.scalamu.compilation.ForgetfulInvoker
import scoverage.IOUtils

import scala.util.control.NonFatal

class InvocationDataReader(dir: Path) {
  import InvocationDataReader._

  def invokedStatements(): Set[Int] = {
    val measurementFiles = IOUtils.findMeasurementFiles(dir.toFile)
    val invocations      = IOUtils.invoked(measurementFiles)
    clearData()
    invocations.toSet
  }

  def clearData(): Unit = {
    clearData(IOUtils.findMeasurementFiles(dir.toFile))
    ForgetfulInvoker.forget()
  }

  private def clearData(files: Seq[File]): Unit = files.foreach { file =>
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
