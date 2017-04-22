package org.scalamu.testing.junit

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RunWithJar extends FlatSpec with Matchers {
  "RunWithInherited" should "run scalatest test with Junit runner" in {
    10 should be > 1
  }
}
