package org.scalamu

import java.io.File
import java.nio.file.Path

package object core {
  def die(failure: RemoteProcessFailure): Nothing = die(failure.exitCode)
  def die(exitValue: Int): Nothing                = sys.exit(exitValue)

  def pathsToString(paths: Traversable[Path]): String =
    paths.foldLeft("")(_ + _ + File.pathSeparator)
}
