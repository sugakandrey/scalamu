package org.scalamu.testapi
package utest

import _root_.utest.TestSuite
import _root_.utest.TestRunner
import _root_.utest.framework.{ExecutionContext, HTree, Result}
import org.scalamu.common.TryBackCompatibility
import org.scalamu.core.ClassName
import org.scalamu.utils.ClassLoadingUtils

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
        andThen { suite => TestRunner.run(suite.tests) }
        andThen converter.fromResult(suiteName)
    )
  }
}
