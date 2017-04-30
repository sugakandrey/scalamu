package org.scalamu.core
package detection

import java.nio.file.Path

import org.scalamu.utils.FileSystemUtils

trait CollectingFileFinder[T] extends FileFinder[T] with FileSystemUtils {
  def predicate: Path => Boolean
  def fromPath: Path => Option[T]

  override def findAll(paths: Set[Path]): List[T] = {
    val files = paths.flatMap(_.filter(predicate))
    files.flatMap(fromPath(_))(collection.breakOut)
  }
}
