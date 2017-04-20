package org.scalamu.testapi.utest

import org.scalamu.testapi._
import _root_.utest.TestSuite
import _root_.utest.framework.{Result, Tree}

trait UTestFramework extends TestingFramework {
  type R = Tree[Result]

  override def name: String          = "utest"
  override def runner: TestRunner[R] = new UTestRunner
  override def filter: TestClassFilter =
    new SuperclassBasedFilter(classOf[TestSuite], this) with IsAModule with HasNoArgConstructor
}

object UTestFramework extends UTestFramework
