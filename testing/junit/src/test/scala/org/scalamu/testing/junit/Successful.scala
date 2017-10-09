package org.scalamu.testing.junit

import org.junit.{Ignore, Test}
import org.junit.Assert._

class Successful {
  @Test
  def testFoo(): Unit = {
    val s = 123
    assertEquals(123, s)
  }

  @Test(expected = classOf[RuntimeException])
  def testBar(): Unit =
    throw new RuntimeException("exception thrown")

  @Ignore("This test is ignored")
  @Test
  def ignored(): Unit =
    assert(1 == 2)
}
