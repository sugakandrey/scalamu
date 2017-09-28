package example

import org.specs2.mutable._
import org.specs2.runner._

class BarSpec extends Specification {
  "example1" >> { Bar.bar(); ko } section("Ignored")
}
