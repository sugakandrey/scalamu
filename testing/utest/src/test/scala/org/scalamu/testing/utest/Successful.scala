package org.scalamu.testing.utest

import utest._
import utest.framework.{Test, Tree}

object Successful extends TestSuite {
  override def tests: Tree[Test] = this {
    val tests = this {
      "aTest" - {
        * - { 1 == 1 }
        * - { 2 == 2 }
        * - { 3 == 3 }
      }
      val x = 1
      'outer {
        val y = x + 1
        'inner {
          val z = y + 1
          'innerest {
            assert(
              x == 1,
              y == 2,
              z == 3
            )
            (x, y, z)
          }
        }
      }
    }
  }
}
