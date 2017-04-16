package org.scalamu.core

import java.nio.file.{Files, Path, StandardOpenOption}

import org.scalamu.utils.ASMUtils
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

final case class ClassFileInfo(
  name: ClassName,
  superClasses: Set[ClassName],
  annotations: Set[ClassName],
  isModule: Boolean,
  source: Option[String]
)

object ClassFileInfo extends ASMUtils {
  private val log = LoggerFactory.getLogger(this.getClass)

  def loadFromPath(path: Path): Option[ClassFileInfo] =
    try {
      val is = Files.newInputStream(path, StandardOpenOption.READ)
      Option(loadClassFileInfo(is))
    } catch {
      case NonFatal(e) =>
        log.error(s"Failed to open InputStream from class file $path. Cause ${e.getMessage}")
        None
    }
}
