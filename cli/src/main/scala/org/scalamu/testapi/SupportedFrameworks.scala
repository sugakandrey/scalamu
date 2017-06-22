package org.scalamu.testapi

import org.scalamu.testapi.junit.JUnitFramework
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

object SupportedFrameworks {
  def allFrameworks: Seq[TestingFramework] = Seq(
    JUnitFramework,
    Specs2Framework,
    ScalaTestFramework,
    UTestFramework
  )

  def frameworkByName: Map[String, TestingFramework] =
    allFrameworks.map(framework => framework.name.toLowerCase -> framework)(collection.breakOut)
}
