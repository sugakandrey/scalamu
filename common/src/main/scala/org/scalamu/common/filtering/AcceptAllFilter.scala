package org.scalamu.common.filtering

case object AcceptAllFilter extends NameFilter {
  override def isSymbolIgnored(symbolName: String): Boolean = false
}
