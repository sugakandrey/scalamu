package org.scalamu.testing.utest

import utest._
import utest.framework.{Test, Tree}

class FlakyThing {
  var runs = 0
  def run(): Unit = {
    runs += 1
    if (runs < 2) throw new Exception("Flaky!")
  }
}

object Retries extends TestSuite.Retries {
  override val utestRetryCount = 3
  val flaky                    = new FlakyThing

  override def tests: Tree[Test] = this {
    'hello {
      flaky.run()
    }
  }
}
