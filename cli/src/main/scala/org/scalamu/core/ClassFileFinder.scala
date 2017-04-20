package org.scalamu.core

import java.nio.file.Path

import org.scalamu.utils.{ClassLoadingUtils, FileSystemUtils}

class ClassFileFinder extends FileFinder[ClassInfo] with FileSystemUtils with ClassLoadingUtils {

  def findAll(paths: Path*): Set[ClassInfo] = {
    val classFiles = paths.flatMap(_.filter(_.isClassFile))
    classFiles.flatMap(ClassInfo.loadFromPath)(collection.breakOut)
  }
}
