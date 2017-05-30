package org.scalamu.testapi
package junit

import _root_.junit.framework.TestCase
import org.junit.Test
import org.junit.runner.{Result, RunWith}

trait JUnitFramework extends TestingFramework {
  type R = Result

  override def name: String          = "JUnit"
  override def runner: TestRunner[R] = new JUnitTestRunner
  override def filter: TestClassFilter = new CompositeTestClassFilter(
    new AnnotationBasedFilter(classOf[Test], this) with NotAModule with NotAbstract,
    new AnnotationBasedFilter(classOf[RunWith], this) with NotAModule with NotAbstract,
    new SuperclassBasedFilter(classOf[TestCase], this) with NotAModule with NotAbstract
  )
}

object JUnitFramework extends JUnitFramework
