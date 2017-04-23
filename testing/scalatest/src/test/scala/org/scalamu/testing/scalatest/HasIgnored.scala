package org.scalamu.testing.scalatest

class HasIgnored extends BaseTrait {
  "HasIgnored" should "not fail with an exception" ignore {
    throw new RuntimeException
  }
}
