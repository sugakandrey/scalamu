package org.scalamu.testing.scalatest

import org.scalatest.{Args, Status, Suite}

class ValidRunner(aClass: Class[_ <: BaseTrait]) extends Suite {
  override def run(testName: Option[String], args: Args): Status = {
    val instance = aClass.getConstructor(classOf[Int]).newInstance(new Integer(42))
    instance.run(None, args)
  }
}
