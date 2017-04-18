package org.scalamu.testapi.specs2

import org.scalamu.testapi._
import org.specs2.specification.core.SpecificationStructure

trait Specs2Framework extends TestingFramework {
  override def name: String       = "Specs2"
  override def runner: TestRunner = new Specs2Runner
  override def filter: TestClassFilter =
    new SuperclassBasedFilter(classOf[SpecificationStructure], this)
}

object Specs2Framework extends Specs2Framework
