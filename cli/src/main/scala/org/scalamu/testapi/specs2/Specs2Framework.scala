package org.scalamu.testapi
package specs2

import org.specs2.specification.core.SpecificationStructure
import org.specs2.specification.process.Stats

class Specs2Framework(override val arguments: String) extends TestingFramework {
  type R = Stats

  override def name: String          = "Specs2"
  override def runner: TestRunner[R] = new Specs2Runner

  override def classFilter: TestClassFilter =
    new SuperclassBasedFilter(classOf[SpecificationStructure], this) with NotAbstract
}

object Specs2Framework extends Specs2Framework("")
