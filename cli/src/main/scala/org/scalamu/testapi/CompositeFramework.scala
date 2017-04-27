package org.scalamu.testapi

import org.scalamu.testapi.junit.JUnitFramework
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

object CompositeFramework {
  val filter = new CompositeTestClassFilter(
    JUnitFramework.filter,
    ScalaTestFramework.filter,
    Specs2Framework.filter,
    UTestFramework.filter
  )
}
