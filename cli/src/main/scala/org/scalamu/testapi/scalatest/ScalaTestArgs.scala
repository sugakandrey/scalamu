package org.scalamu.testapi.scalatest

import org.scalatest.ConfigMap

final case class ScalaTestArgs(
  configMap: ConfigMap,
  tagsToInclude: Set[String] = Set.empty,
  tagsToExclude: Set[String] = Set.empty,
  spanScaleFactor: Double = 1
)
