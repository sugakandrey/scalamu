package org.scalamu.core
package detection

import java.nio.file.Path

import org.scalamu.core.api.SourceInfo

class SourceFileFinder extends CollectingFileFinder[SourceInfo] {
  override def predicate: (Path) => Boolean           = _.isSourceFile
  override def fromPath: (Path) => Option[SourceInfo] = SourceInfo.fromPath
}
