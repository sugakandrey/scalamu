package org.scalamu.testutil

import java.nio.file.{Files, Path}

import org.scalamu.utils.FileSystemUtils

trait FileSystemSpec { this: FileSystemUtils =>

  def createTempFile(prefix: String, suffix: String): RichPath =
    setDeleteOnSave(Files.createTempFile(prefix, suffix))

  def createTempFile(prefix: String, suffix: String, dir: Path): RichPath =
    setDeleteOnSave(Files.createTempFile(dir, prefix, suffix))

  def creteTempDirectory(name: String, dir: Path): RichPath =
    setDeleteOnSave(Files.createTempDirectory(dir, name))

  def createTempDirectory(name: String): RichPath =
    setDeleteOnSave(Files.createTempDirectory(name))

  private def setDeleteOnSave(path: Path): RichPath = { path.toFile.deleteOnExit(); path }
}
