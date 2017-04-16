package org.scalamu.core

import java.nio.file.Path

import org.scalamu.utils.{ClassLoadingUtils, FileSystemUtils}

trait ClassFileFinder
    extends FileFinder[ClassFileInfo]
    with FileSystemUtils
    with ClassLoadingUtils {

  def findAll(paths: Path*): Set[ClassFileInfo] = {
    val classFiles = paths.filter(_.isClassFile)
    withContextClassLoader(loaderFromPaths(paths))(
      classFiles.flatMap(ClassFileInfo.loadFromPath)(collection.breakOut)
    )
  }
}

class TestClassFileFinder(testClassCues: TestClassFilter) extends ClassFileFinder {
  override def findAll(paths: Path*): Set[ClassFileInfo] = 
    super.findAll(paths: _*).filter(testClassCues)
}
