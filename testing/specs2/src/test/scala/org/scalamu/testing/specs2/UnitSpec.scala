package org.scalamu.testing.specs2

import org.specs2.mutable.Specification

class UnitSpec extends Specification {
  "this is my specification" >> {
    "where example 1 must be true" >> {
      1 must_== 1
    }
    "where example 2 must be true" >> {
      2 must_== 2
    }
  }
}
