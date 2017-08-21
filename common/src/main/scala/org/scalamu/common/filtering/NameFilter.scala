package org.scalamu.common.filtering

trait NameFilter extends (String => Boolean) {
  def accepts: String => Boolean

  override def apply(symbolName: String): Boolean = accepts(symbolName)
}
