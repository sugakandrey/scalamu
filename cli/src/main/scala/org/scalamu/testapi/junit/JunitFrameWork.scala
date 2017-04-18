package org.scalamu.testapi
package junit

import org.junit.Test
import org.junit.runner.RunWith

trait JunitFrameWork extends TestingFramework { self =>
  override def name: String       = "JUnit"
  override def runner: TestRunner = new JunitTestRunner
  override def filter: TestClassFilter = new CompoundTestClassFilter(
    new AnnotationBasedFilter(classOf[Test], this) with NotAModule,
    new AnnotationBasedFilter(classOf[RunWith], this) with NotAModule
  )
}

object JunitFrameWork extends JunitFrameWork
