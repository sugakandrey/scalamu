package org.scalamu.testing.junit

import org.junit.Test

class Failed {
  @Test
  def foo(): Unit =
    assert(1 == 1)

  @Test
  def bar(): Unit = throw new RuntimeException
}
