package org.scalamu.core.testapi
package specs2

import org.specs2.specification.core.SpecificationStructure
import org.specs2.specification.process.Stats

class Specs2Framework(override val arguments: String) extends TestingFramework {
  type R = Stats

  override def name: String          = "Specs2"
  override def runner: TestRunner[R] = new Specs2Runner(arguments)

  override def classFilter: TestClassFilter =
    new SuperclassBasedFilter(classOf[SpecificationStructure], this) with NotAbstract
}

object Specs2Framework extends Specs2Framework("") {
  def apply(arguments: String): Specs2Framework = new Specs2Framework(arguments)
}
