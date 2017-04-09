package org.scalamu.plugin.util

import scala.tools.nsc.Global

trait CompilerAccess {
  val global: Global
}
