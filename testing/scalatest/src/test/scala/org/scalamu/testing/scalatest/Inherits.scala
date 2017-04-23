package org.scalamu.testing.scalatest

class Inherits extends BaseTrait {
  "Inherits" should "check foo" in {
    "foo" should startWith ("f")
  }
}
