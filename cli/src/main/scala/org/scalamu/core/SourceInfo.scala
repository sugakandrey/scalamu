package org.scalamu.core

import java.nio.file.{Files, Path}

import org.scalamu.utils.FileSystemUtils.RichPath

final case class SourceInfo(
  name: Path,
  dir: Path
) {
  def fullPath: Path = dir / name
}

object SourceInfo {
  def fromPath(path: Path): Option[SourceInfo] =
    if (Files.exists(path) && path.isSourceFile)
      Some(SourceInfo(path.getFileName, path.getParent))
    else None
}
