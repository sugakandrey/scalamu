package org.scalamu.testing.scalatest

import org.scalatest.{FlatSpec, Matchers}

class FailedFlat extends FlatSpec with Matchers {
  "FailedTest" should "fail" in {
    0 should ===(1)
  }
}
