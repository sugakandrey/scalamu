package org.scalamu.testapi.utest

import org.scalamu.testapi._
import _root_.utest.TestSuite

trait UTestFramework extends TestingFramework {
  override def name: String       = "utest"
  override def runner: TestRunner = new UTestRunner
  override def filter: TestClassFilter =
    new SuperclassBasedFilter(classOf[TestSuite], this) with IsAModule with HasNoArgConstructor
}

object UTestFramework extends UTestFramework
