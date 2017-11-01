package org.scalamu.core.testapi
package junit

import org.junit.runner.{JUnitCore, Result}
import org.scalamu.common.TryBackCompatibility
import org.scalamu.core.api.ClassName

class JUnitRunner(override val arguments: String) extends TestRunner[Result] with TryBackCompatibility {

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
