package example.foo

import org.scalatest.{FlatSpec, Matchers, Tag}

class FooSpec extends FlatSpec with Matchers {
  it should "foo" in {
    Foo.foo()
  }
}
