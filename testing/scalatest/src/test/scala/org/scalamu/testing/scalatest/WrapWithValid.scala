package org.scalamu.testing.scalatest

import org.scalatest.WrapWith

@WrapWith(classOf[ValidRunner])
class WrapWithValid(i: Int) extends BaseTrait {
  "WrapWithValid" should "be able to run, even though it has no no-arg constructor" in {
    1 should ===(1)
  }
}
