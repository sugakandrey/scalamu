package example

import org.scalatest.{FlatSpec, Matchers, Tag}

object Ignored extends Tag("tags.Ignored")

class BazSpec extends FlatSpec with Matchers {
  it should "123" taggedAs (Ignored) in {
    1 should ===(2)
  }
}
