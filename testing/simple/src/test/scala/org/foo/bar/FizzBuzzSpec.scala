package org.foo.bar

import org.scalatest.{FunSuiteLike, Matchers}

class FizzBuzzSpec extends Matchers with FunSuiteLike {
  test("FizzBuzz result should have length == i") {
    val i  = 10
    val fb = FizzBuzz(i)
    assert(fb.fizzBuzz().size == i)
  }

  test("Every 15th element should equal 'FizzBuzz'") {
    val i    = 100
    val fb   = FizzBuzz(i).fizzBuzz()
    val fbzs = (15 to i by 15).map(fb)
    assert(fbzs.forall(_ == "FizzBuzz"))
  }
}
