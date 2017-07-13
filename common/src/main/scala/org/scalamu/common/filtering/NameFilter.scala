package org.scalamu.common.filtering

trait NameFilter extends (String => Boolean) {
  override def apply(symbolName: String): Boolean = isNameIgnored(symbolName)
  def isNameIgnored(symbolName: String): Boolean
}
