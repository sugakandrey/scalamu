package org.scalamu.testapi.junit

import org.junit.runner.{JUnitCore, Result}
import org.scalamu.core.ClassName
import org.scalamu.testapi.{SuiteResultTypeConverter, TestRunner, TestSuiteResult}

class JUnitTestRunner extends TestRunner[Result] {
  override protected def converter: SuiteResultTypeConverter[Result] = JUnitConverters

  override def run(suite: ClassName): TestSuiteResult =
    suite.loadFromContextClassLoader.fold(
      TestSuiteResult.Aborted(suite, _), { cl: Class[_] =>
        JUnitCore.runClasses(cl)
      } andThen converter.fromResult(suite)
    )
}
