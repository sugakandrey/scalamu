package org.scalamu.testapi
package specs2

import org.scalamu.core.ClassName
import org.specs2.specification.process.Stats

class Specs2Converters(notifier: Specs2Notifier) extends SuiteResultTypeConverter[Stats] {
  override def fromResult(suite: ClassName)(result: Stats): TestSuiteResult =
    if (!result.hasFailuresOrErrors)
      SuiteSuccess(suite, notifier.duration)
    else
      TestsFailed(suite, notifier.getFailures)
}
