package org.scalamu.core.api

import java.nio.file.Path

import cats.syntax.option._
import com.typesafe.scalalogging.Logger
import org.scalamu.common.TryBackCompatibility
import org.scalamu.core.utils.FileSystemUtils

final case class ClassInfo(
  name: ClassName,
  superClasses: Set[ClassName],
  annotations: Set[ClassName],
  isModule: Boolean,
  hasNoArgConstructor: Boolean,
  isAbstract: Boolean,
  source: Option[String]
)

object ClassInfo extends TryBackCompatibility {
  import FileSystemUtils._
  import org.scalamu.core.utils.ASMUtils._
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
