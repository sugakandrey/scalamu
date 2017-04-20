package org.scalamu.testapi.junit

import org.junit.runner.{JUnitCore, Result}
import org.scalamu.core.ClassInfo
import org.scalamu.testapi.{SuiteResultTypeConverter, TestRunner, TestSuiteResult}

class JUnitTestRunner extends TestRunner[Result] {
  override protected def converter: SuiteResultTypeConverter[Result] = JUnitConverters

  override def run(info: ClassInfo): TestSuiteResult =
    info.name.loadFromContextClassLoader.fold(
      TestSuiteResult.Aborted(info.name, _), { cl: Class[_] =>
        JUnitCore.runClasses(cl)
      } andThen converter.fromResult(info.name)
    )
}
