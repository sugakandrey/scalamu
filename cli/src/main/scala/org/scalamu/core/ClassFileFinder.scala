package org.scalamu.core

import java.nio.file.Path

import org.scalamu.utils.{ClassLoadingUtils, FileSystemUtils}

class ClassFileFinder
    extends FileFinder[ClassFileInfo]
    with FileSystemUtils
    with ClassLoadingUtils {

  def findAll(paths: Path*): Set[ClassFileInfo] = {
    val classFiles = paths.flatMap(_.filter(_.isClassFile))
    classFiles.flatMap(ClassFileInfo.loadFromPath)(collection.breakOut)
  }
}

class TestClassFileFinder(testClassCues: TestClassFilter) extends ClassFileFinder {
  override def findAll(paths: Path*): Set[ClassFileInfo] =
    super.findAll(paths: _*).filter(testClassCues)
}
