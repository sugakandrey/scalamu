package org.scalamu.testapi

import cats.Foldable
import cats.implicits._
import org.scalamu.common.filtering.{NameFilter, RegexFilter}
import org.scalamu.core.{ClassInfo, ClassName}

import scala.util.matching.Regex

/**
 * Allows for filtering test classes out of all class files.
 */
trait TestClassFilter extends (ClassInfo => Option[TestClassInfo]) {
  protected def framework: TestingFramework

  protected def predicate: ClassInfo => Boolean

  override def apply(info: ClassInfo): Option[TestClassInfo] =
    if (predicate(info)) TestClassInfo(info, framework).some
    else none
}

object TestClassFilter {
  def forFrameworks(
    frameworks: Seq[TestingFramework],
    inclusionRules: Seq[Regex] = Seq.empty
  ): TestClassFilter =
    new CompositeTestClassFilter(frameworks.map(_.classFilter): _*) with HasAppropriateName {
      override val nameFilter: NameFilter = RegexFilter(inclusionRules: _*)
    }
}

/**
 * Composite filter, that aggregates filtering results of a set of other filters.
 * @param filters to be aggregated
 */
class CompositeTestClassFilter(filters: TestClassFilter*) extends TestClassFilter {
  override protected def framework: TestingFramework       = ???
  override protected def predicate: (ClassInfo) => Boolean = ???
  override def apply(info: ClassInfo): Option[TestClassInfo] =
    Foldable[List].foldK(filters.map(_.apply(info))(collection.breakOut))
}

/**
 * Filters class files with respect to the specified annotation class.
 *
 * @param annotation to search for
 * @param framework corresponding test framework
 */
class AnnotationBasedFilter(val annotation: Class[_], override val framework: TestingFramework)
    extends TestClassFilter {
  override protected lazy val predicate: (ClassInfo) => Boolean =
    _.annotations.contains(ClassName.forClass(annotation))
}

/**
 * Filters class files with respect to the specified superclass or superinterface.
 *
 * @param testClass class or interface to search for
 * @param framework corresponding test framework
 */
class SuperclassBasedFilter(val testClass: Class[_], override val framework: TestingFramework) extends TestClassFilter {
  override protected lazy val predicate: (ClassInfo) => Boolean =
    _.superClasses.contains(ClassName.forClass(testClass))
}
