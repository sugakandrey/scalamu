package example.baz

import org.specs2.mutable._

class BazSpec extends Specification {
  "example1" >> { Baz.baz(); ok } 
}
