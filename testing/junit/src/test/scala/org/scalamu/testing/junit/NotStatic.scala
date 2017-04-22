package org.scalamu.testing.junit

import org.junit.Test
import org.junit.BeforeClass

class NotStatic {
  @BeforeClass
  def invalidNonStatic(): Unit = {
    println("Totally invalid @BeforeClassMethod.")
  }

  @Test
  def foo(): Unit = assert(1 == 1)
}
