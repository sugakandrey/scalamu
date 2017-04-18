package org.baz.qux

import org.junit.Test
import org.junit.Assert._

class FibsTest {
  @Test
  def testRecursive(): Unit = {
    val fibs = Fibs.fibsRecursive(11)
    assertEquals(89, fibs)
  }

  @Test
  def testIterative(): Unit = {
    val fibs = Fibs.fibsIterative(12)
    assertEquals(144, fibs)
  }
}
