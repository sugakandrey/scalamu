package org.scalamu.testing.junit

import org.junit.Test
import org.junit.Assert._
import org.junit.{AfterClass, BeforeClass}

class HasFixture {
  import HasFixture._

  @Test
  def foo(): Unit = assertEquals(resource.length, 5)
}

object HasFixture {
  var resource: String = _

  @BeforeClass
  def setUp(): Unit =
    resource = "hello"

  @AfterClass
  def tearDown(): Unit =
    resource = null
}
