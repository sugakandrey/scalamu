package org.scalamu.common.filtering

case object AcceptAllFilter extends NameFilter {
  override def accepts: (String) => Boolean = Function.const(true)
}
