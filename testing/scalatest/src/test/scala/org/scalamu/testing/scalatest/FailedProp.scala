package org.scalamu.testing.scalatest

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.collection.immutable._

class FailedProp extends PropSpec with TableDrivenPropertyChecks with Matchers {
  val examples =
    Table(
      "set",
      BitSet(1),
      HashSet(1),
      TreeSet(1)
    )

  property("an empty Set should have size 1") {
    forAll(examples) { set =>
      set.size should be(0)
    }
  }
}
