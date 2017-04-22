package org.scalamu.testing.junit

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.{Parameterized => JUParameterized}
import org.junit.runners.Parameterized.Parameters

@RunWith(classOf[JUParameterized])
class RunWithParam(x: java.lang.Integer) {
  @Test
  def test(): Unit = {
    assert(x > 0)
  }
}

object RunWithParam {
  @Parameters
  def parameters: java.util.Collection[Array[java.lang.Integer]] = {
    val list = new java.util.ArrayList[Array[java.lang.Integer]]()
    (1 to 10).foreach(n => list.add(Array(n)))
    list
  }
}
