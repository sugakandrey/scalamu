package org.scalamu.testapi.junit

import org.junit.runner.{JUnitCore, Result}
import org.scalamu.core.ClassFileInfo
import org.scalamu.testapi.{InternalAPIConverter, TestRunner, TestSuiteResult}

class JUnitTestRunner extends TestRunner[Result] {
  override protected def converter: InternalAPIConverter[Result] = JUnitConverters

  override def run(info: ClassFileInfo): TestSuiteResult =
    info.name.loadFromContextClassLoader.fold(
      TestSuiteResult.Aborted(info.name, _), { cl: Class[_] =>
        JUnitCore.runClasses(cl)
      } andThen converter.fromResult(info.name)
    )
}
