package org.baz.qux

import utest._
import utest.framework.{Test, Tree}

object FibsMicroTest extends TestSuite {
  override def tests: Tree[Test] = this {
    'testIterative {
      Fibs.fibsIterative(12) ==> 144
    }
    'testRecursive {
      Fibs.fibsRecursive(11) ==> 89
    }
  }
}
