package org.foo.bar

import org.baz.qux.FibsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(classOf[Suite])
@SuiteClasses(Array(classOf[FizzBuzzTest], classOf[FibsTest]))
class FizzBuzzTest {
  println("Has @RunWith annotation.")
}
