package org.scalamu.testapi
package utest

import _root_.utest.TestSuite
import _root_.utest.framework.{ExecutionContext, Result, Tree}
import org.scalamu.core.ClassName
import org.scalamu.utils.ClassLoadingUtils

import scala.reflect.runtime.universe
import scala.util.Try

class UTestRunner extends TestRunner[Tree[Result]] {

  override protected def converter: SuiteResultTypeConverter[Tree[Result]] = UTestConverters

  override def run(suiteName: ClassName): TestSuiteResult = {
    val mirror       = universe.runtimeMirror(ClassLoadingUtils.contextClassLoader)
    val moduleSymbol = Try(mirror.staticModule(suiteName.fullName))
    implicit val ec  = ExecutionContext.RunNow
    moduleSymbol.fold(
      TestSuiteResult.Aborted(suiteName, _),
      mirror.reflectModule _
        andThen { _.instance.asInstanceOf[TestSuite] }
        andThen { suite => suite.tests.run(wrap = suite.utestWrap(_)) }
        andThen converter.fromResult(suiteName)
    )
  }
}
