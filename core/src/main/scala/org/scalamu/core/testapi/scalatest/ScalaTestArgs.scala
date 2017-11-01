package org.scalamu.core.testapi
package scalatest

import org.scalatest.ConfigMap

final case class ScalaTestArgs(
  configMap: ConfigMap,
  tagsToInclude: Set[String] = Set.empty,
  tagsToExclude: Set[String] = Set.empty,
  spanScaleFactor: Double = 1
)
