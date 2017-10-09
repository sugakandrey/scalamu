package org.scalamu.testing.scalatest

import org.scalatest.{FlatSpec, Matchers}

class Flat extends FlatSpec with Matchers {
  "SuccessfulTest" should "be successful" in {
    val a = 123
    a should be > 10
  }

  it should "not fail" in {
    intercept[RuntimeException] {
      throw new RuntimeException
    }
  }
}
