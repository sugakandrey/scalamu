package example

import org.specs2.mutable._

class BarSpec extends Specification {
  "example1" >> { Bar.bar(); ok }
}
