package example.bar

import utest._

object BarUTest extends TestSuite {
  override def tests = this {
    'test1 - {
      Bar.bar()
    }
  }
}
