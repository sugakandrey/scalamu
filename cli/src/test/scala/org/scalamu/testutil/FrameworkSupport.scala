package org.scalamu.testutil

import org.scalamu.testapi.{CompositeTestClassFilter, TestClassFilter}
import org.scalamu.testapi.junit.JUnitFramework
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

trait FrameworkSupport {
  def filter: TestClassFilter = new CompositeTestClassFilter(
    JUnitFramework.filter,
    ScalaTestFramework.filter,
    Specs2Framework.filter,
    UTestFramework.filter
  )
}
