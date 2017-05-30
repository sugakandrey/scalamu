package org.scalamu.core

import java.nio.file.Path

import cats.syntax.option._
import com.typesafe.scalalogging.Logger
import org.scalamu.utils.FileSystemUtils

final case class ClassInfo(
  name: ClassName,
  superClasses: Set[ClassName],
  annotations: Set[ClassName],
  isModule: Boolean,
  hasNoArgConstructor: Boolean,
  isAbstract: Boolean,
  source: Option[String]
)

object ClassInfo {
  import org.scalamu.utils.ASMUtils._
  import FileSystemUtils._
  private val log = Logger[ClassInfo]

  def loadFromPath(path: Path): Option[ClassInfo] =
    path.toInputStream
      .flatMap(loadClassFileInfo)
      .fold(
        exc => {
          log.error(
            s"Failed to load class file $path. Cause: $exc. " +
              s"Make sure the classpath is set correctly, prior to calling loadFromPath()"
          )
          None
        },
        _.some
      )
}
