package org.scalamu.testing.utest
  
import utest._
import utest.framework.{Test, Tree}

object Failed extends TestSuite {
  override def tests: Tree[Test] = this {
    "stringy-named test" - {
      val a = 10
      a ==> 10
    }
    
    "failing test" - {
      1 ==> 2
    }
  }
}
