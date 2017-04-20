package org.scalamu.testapi
package utest

import _root_.utest.TestSuite
import _root_.utest.framework.{ExecutionContext, Result, Tree}
import org.scalamu.core.ClassFileInfo

import scala.reflect.runtime.universe
import scala.util.Try

class UTestRunner extends TestRunner[Tree[Result]] {

  override protected def converter: InternalAPIConverter[Tree[Result]] = UTestConverters

  override def run(info: ClassFileInfo): TestSuiteResult = {
    val name         = info.name
    val mirror       = universe.runtimeMirror(Thread.currentThread().getContextClassLoader)
    val moduleSymbol = Try(mirror.staticModule(name.fullName))
    implicit val ec  = ExecutionContext.RunNow
    moduleSymbol.fold(
      TestSuiteResult.Aborted(name, _),
      mirror.reflectModule _
        andThen { _.instance.asInstanceOf[TestSuite] }
        andThen { _.tests.run() }
        andThen converter.fromResult(name)
    )
  }
}
