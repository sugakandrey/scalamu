package org.scalamu.testutil

import org.scalamu.testapi.{CompoundTestClassFilter, TestClassFilter}
import org.scalamu.testapi.junit.JunitFrameWork
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

trait FrameworkSupport
    extends JunitFrameWork
    with ScalaTestFramework
    with Specs2Framework
    with UTestFramework {

  override def filter: TestClassFilter = new CompoundTestClassFilter(
    JunitFrameWork.filter,
    ScalaTestFramework.filter,
    Specs2Framework.filter,
    UTestFramework.filter
  )
}
