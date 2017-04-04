package org.scalamu.plugin.mutations

import scala.tools.nsc.Global

trait CompilerAccess {
  val global: Global
}
