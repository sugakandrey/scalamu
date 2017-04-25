package org.scalamu.testing.specs2

import org.specs2.mutable.Specification

class Failed extends Specification {
  def foo(): Unit = throw new RuntimeException

  "this is my specification" >> {
    "where example 1 must be true" >> {
      1.must_==(1)
    }
    "where example 2 must be true" >> {
      2.must_==(2)
    }
    "where example 2 must fail" >> {
      1.must_==(2)
    }
    "where example 3 must throw" >> {
      foo() must not(throwA[RuntimeException])
    }
  }
}
