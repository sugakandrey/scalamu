package org.scalamu.core
package detection

import java.nio.file.Path

class ClassFileFinder extends CollectingFileFinder[ClassInfo] {
  override def predicate: (Path) => Boolean          = _.isClassFile
  override def fromPath: (Path) => Option[ClassInfo] = ClassInfo.loadFromPath
}
