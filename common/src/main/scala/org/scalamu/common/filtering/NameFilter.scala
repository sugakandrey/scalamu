package org.scalamu.common.filtering

trait NameFilter {
  def accepts: String => Boolean

  def apply(symbolName: String): Boolean = accepts(symbolName)
}
