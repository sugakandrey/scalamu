package org.scalamu.testapi

import java.nio.file.Path

import org.scalamu.core.{ClassFileFinder, FileFinder}

class TestClassFileFinder(filter: TestClassFilter) extends FileFinder[TestClassInfo] {
  override def findAll(paths: Path*): Set[TestClassInfo] =
    new ClassFileFinder().findAll(paths: _*).flatMap(filter(_))
}
