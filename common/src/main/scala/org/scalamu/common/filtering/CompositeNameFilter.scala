package org.scalamu.common.filtering

class CompositeNameFilter(filters: NameFilter*) extends NameFilter {
  override def accepts: (String) => Boolean = name => filters.forall(_.accepts(name))
}
