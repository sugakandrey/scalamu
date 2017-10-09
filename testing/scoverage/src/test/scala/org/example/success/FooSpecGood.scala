package org.example.success

import org.example.Foo
import org.scalatest.{FlatSpec, Matchers}

class FooSpecGood extends FlatSpec with Matchers {
  "Foo" should "do foo()" in {
    val foo = Foo(123, s = None)
    foo.foo() should ===(-123)
  }

  it should "do bar()" in {
    val foo = Foo(123, s = None)
    foo.bar(1, 2) should be < 0
  }

  it should "do qux()" in {
    val foo = Foo(42, s = None)
    foo.qux() should have size 0
  }
}
