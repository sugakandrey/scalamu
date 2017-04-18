package org.scalamu.testapi

import cats.Foldable
import cats.implicits._
import org.scalamu.core.{ClassFileInfo, ClassName}

trait TestClassFilter extends (ClassFileInfo => Option[TestClassFileInfo]) {
  protected def framework: TestingFramework

  protected def predicate: ClassFileInfo => Boolean

  override def apply(info: ClassFileInfo): Option[TestClassFileInfo] =
    if (predicate(info)) TestClassFileInfo(info, framework).some
    else none
}

class CompoundTestClassFilter(filters: TestClassFilter*) extends TestClassFilter {
  override protected def framework: TestingFramework           = ???
  override protected def predicate: (ClassFileInfo) => Boolean = ???
  override def apply(info: ClassFileInfo): Option[TestClassFileInfo] =
    Foldable[List].foldK(filters.map(_.apply(info))(collection.breakOut))
}

class AnnotationBasedFilter(val annotation: Class[_], override val framework: TestingFramework)
    extends TestClassFilter {
  override protected def predicate: (ClassFileInfo) => Boolean =
    _.annotations.contains(ClassName.forClass(annotation))
}

class SuperclassBasedFilter(val testClass: Class[_], override val framework: TestingFramework)
    extends TestClassFilter {
  override protected def predicate: (ClassFileInfo) => Boolean =
    _.superClasses.contains(ClassName.forClass(testClass))
}
