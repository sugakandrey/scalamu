package org.scalamu.testapi

import java.nio.file.Path

import org.scalamu.core.ClassInfo
import org.scalamu.core.detection.CollectingFileFinder

class TestClassFileFinder(filter: TestClassFilter) extends CollectingFileFinder[TestClassInfo] {
  override def predicate: (Path) => Boolean = _.isClassFile
  override def fromPath: (Path) => Option[TestClassInfo] =
    ClassInfo.loadFromPath _ andThen { _.flatMap(filter) }
}
