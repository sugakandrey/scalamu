package org.baz.qux

import org.scalatest.{FlatSpec, Matchers}

class FibsSpec extends FlatSpec with Matchers {
  "Fibs" should "calculate 10th fibonacci number" in {
    val fibs     = Fibs.fibsRecursive(10)
    val fibsIter = Fibs.fibsIterative(10)
    fibs should ===(55)
    fibsIter should ===(55)
  }
}
