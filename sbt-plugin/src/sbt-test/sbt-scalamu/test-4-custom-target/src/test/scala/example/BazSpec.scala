package example

import org.scalatest.{FlatSpec, Matchers, Tag}

class BazSpec extends FlatSpec with Matchers {
  it should "123" in {
    Baz.baz()
  }
}
