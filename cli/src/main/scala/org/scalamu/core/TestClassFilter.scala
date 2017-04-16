package org.scalamu.core

trait TestClassFilter extends (ClassFileInfo => Boolean) {
  override def apply(info: ClassFileInfo): Boolean
}

class CompoundTestClassFilter(filters: Seq[TestClassFilter]) extends TestClassFilter {
  override def apply(info: ClassFileInfo): Boolean =
    filters.map(_.apply(info)).fold(false)(_ || _)
}
