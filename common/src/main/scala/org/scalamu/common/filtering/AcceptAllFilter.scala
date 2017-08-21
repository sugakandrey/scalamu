package org.scalamu.common.filtering

case object AcceptAllFilter extends NameFilter {
  override protected val accepts: (String) => Boolean = Function.const(true)
}
