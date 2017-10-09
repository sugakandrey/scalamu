package org.scalamu.testing.specs2

import org.specs2.Specification
import org.specs2.specification.BeforeAfterAll

class HasFixture extends Specification with BeforeAfterAll {
  private var a = 123
  override def beforeAll(): Unit =
    a = 456

  override def afterAll(): Unit =
    a = 789

  override def is = s2"""

 this is my specification
   where example 1 must be true           $e1
                                          """

  def e1 = a.must_==(456)
}
