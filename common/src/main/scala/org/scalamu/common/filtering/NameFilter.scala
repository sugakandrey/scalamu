package org.scalamu.common.filtering

trait NameFilter extends (String => Boolean) {
  override def apply(symbolName: String): Boolean = isSymbolIgnored(symbolName)
  def isSymbolIgnored(symbolName: String): Boolean
}
