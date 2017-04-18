package org.scalamu.testapi.scalatest

import org.scalamu.testapi._
import org.scalatest.{Suite, WrapWith}

trait ScalaTestFramework extends TestingFramework {
  override def name: String       = "ScalaTest"
  override def runner: TestRunner = new ScalaTestRunner
  override def filter: TestClassFilter = new CompoundTestClassFilter(
    new SuperclassBasedFilter(classOf[Suite], this) with NotAModule with HasNoArgConstructor,
    new SuperclassBasedFilter(classOf[WrapWith], this) with NotAModule
  )
}

object ScalaTestFramework extends ScalaTestFramework
