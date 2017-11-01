package org.scalamu.core.testapi
package scalatest

import org.scalatest.{Status, Suite, WrapWith}

class ScalaTestFramework(override val arguments: String) extends TestingFramework {
  type R = Status

  override def name: String          = "ScalaTest"
  override def runner: TestRunner[R] = new ScalaTestRunner(arguments)

  override def classFilter: TestClassFilter = new CompositeTestClassFilter(
    new SuperclassBasedFilter(classOf[Suite], this) with NotAModule with HasNoArgConstructor with NotAbstract,
    new SuperclassBasedFilter(classOf[WrapWith], this) with NotAModule with NotAbstract
  )
}

object ScalaTestFramework extends ScalaTestFramework("") {
  def apply(arguments: String): ScalaTestFramework = new ScalaTestFramework(arguments)
}
