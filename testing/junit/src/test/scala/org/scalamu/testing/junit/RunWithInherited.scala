package org.scalamu.testing.junit

class RunWithInherited extends RunWithJar {
  "RunWithInherited" should "run scalatest test, which inherits from @RunWith test" in {
    "hello" should startWith ("hell")
  }
}
