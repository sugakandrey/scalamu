package org.scalamu.testapi.specs2

import org.scalamu.core.ClassName
import org.scalamu.testapi.{SuiteResultTypeConverter, TestSuiteResult}
import org.specs2.specification.process.Stats

class Specs2Converters(notifier: Specs2Notifier) extends SuiteResultTypeConverter[Stats] {
  override def fromResult(suite: ClassName)(result: Stats): TestSuiteResult =
    if (!result.hasFailuresOrErrors)
      TestSuiteResult.Successful(suite, notifier.duration)
    else
      TestSuiteResult.Failed(suite, notifier.getFailures)
}
