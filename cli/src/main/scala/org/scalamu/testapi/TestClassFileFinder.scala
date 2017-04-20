package org.scalamu.testapi

import java.nio.file.Path

import org.scalamu.core.{ClassFileFinder, FileFinder}

class TestClassFileFinder(filter: TestClassFilter) extends FileFinder[TestClassInfo] {
  override def findAll(paths: Set[Path]): Set[TestClassInfo] =
    new ClassFileFinder().findAll(paths).flatMap(filter(_))
}
