package org.scalamu.common.filtering

case object AcceptAllFilter extends NameFilter {
  override def isNameIgnored(symbolName: String): Boolean = false
}
