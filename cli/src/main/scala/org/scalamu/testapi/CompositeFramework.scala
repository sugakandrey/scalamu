package org.scalamu.testapi

import org.scalamu.common.filtering.{NameFilter, RegexBasedFilter}
import org.scalamu.testapi.junit.JUnitFramework
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

import scala.util.matching.Regex

class CompositeFramework(
  frameworksArguments: Map[String, String] = Map.empty,
  filters: Seq[Regex] = Seq.empty
) {
  private def getArgs(framework: TestingFramework): String =
    frameworksArguments.getOrElse(framework.name.toLowerCase, "")

  val filter = new CompositeTestClassFilter(
    new JUnitFramework(getArgs(JUnitFramework)).classFilter,
    new ScalaTestFramework(getArgs(ScalaTestFramework)).classFilter,
    new Specs2Framework(getArgs(Specs2Framework)).classFilter,
    new UTestFramework(getArgs(UTestFramework)).classFilter
  ) with HasAppropriateName {
    override val nameFilter: NameFilter = RegexBasedFilter(filters: _*)
  }
}
