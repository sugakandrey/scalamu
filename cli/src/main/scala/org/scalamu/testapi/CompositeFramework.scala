package org.scalamu.testapi

import org.scalamu.common.filtering.{NameFilter, RegexBasedFilter}
import org.scalamu.testapi.junit.JUnitFramework
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

import scala.util.matching.Regex

class CompositeFramework(filters: Regex*) {
  val filter = new CompositeTestClassFilter(
    JUnitFramework.filter,
    ScalaTestFramework.filter,
    Specs2Framework.filter,
    UTestFramework.filter
  ) with HasAppropriateName {
    override val nameFilter: NameFilter = RegexBasedFilter(filters: _*)
  }
}
