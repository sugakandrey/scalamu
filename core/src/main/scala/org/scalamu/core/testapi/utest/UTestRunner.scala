package org.scalamu.core.testapi
package utest

import _root_.utest.{TestRunner, TestSuite}
import _root_.utest.framework.{HTree, Result}
import org.scalamu.common.TryBackCompatibility
import org.scalamu.core.api.ClassName
import org.scalamu.core.utils.ClassLoadingUtils

import scala.reflect.runtime.universe
import scala.util.Try

class UTestRunner(override val arguments: String) extends TestRunner[HTree[String, Result]] with TryBackCompatibility {

  override protected def converter: SuiteResultTypeConverter[HTree[String, Result]] = UTestConverters

  override def run(suiteName: ClassName): TestSuiteResult = {
    val mirror                                    = universe.runtimeMirror(ClassLoadingUtils.contextClassLoader)
    val moduleSymbol                              = Try(mirror.staticModule(suiteName.fullName))
    
    moduleSymbol.fold(
      SuiteExecutionAborted(suiteName, _),
      mirror.reflectModule _
        andThen { _.instance.asInstanceOf[TestSuite] }
        andThen { suite => TestRunner.run(suite.tests, executor = suite) }
        andThen converter.fromResult(suiteName)
    )
  }
}
