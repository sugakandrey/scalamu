package org.scalamu.core
package detection

import java.nio.file.Path

trait FileFinder[T] {
  def findAll(paths: Set[Path]): Set[T]
}
