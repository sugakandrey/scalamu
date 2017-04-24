package org.scalamu.testing.specs2

import org.specs2.mutable.Specification

class HasIgnored extends Specification {
  "this example fails for now" in {
    1 must_== 2
  }.pendingUntilFixed

  // or, with a more specific message
  "this example fails for now" in {
    1 must_== 2
  }.pendingUntilFixed("ISSUE-123")
}
