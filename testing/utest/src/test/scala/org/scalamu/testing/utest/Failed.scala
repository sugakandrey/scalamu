package org.scalamu.testing.utest

import utest._

object Failed extends TestSuite {
  override def tests: Tests = this {
    "stringy-named test" - {
      val a = 10
      a ==> 10
    }

    "failing test" - {
      1 ==> 2
    }
  }
}
