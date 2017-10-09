package example

import utest._

object BarTest extends TestSuite {
  override def tests = this {
    'test1 - {
      Bar.bar()
    }
  }
}
