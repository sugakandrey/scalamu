package org.scalamu.testapi
package junit

import org.junit.runner.{JUnitCore, Result}
import org.scalamu.core.ClassName

class JUnitRunner extends TestRunner[Result] {
  override protected def converter: SuiteResultTypeConverter[Result] = JUnitConverters

  override def run(suite: ClassName): TestSuiteResult = {
    JUnitCore.runClasses()
    suite.loadFromContextClassLoader.fold(
      SuiteExecutionAborted(suite, _), { cl: Class[_] =>
        JUnitCore.runClasses(cl)
      } andThen converter.fromResult(suite)
    )
  }
}
