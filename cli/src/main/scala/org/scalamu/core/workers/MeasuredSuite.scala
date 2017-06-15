package org.scalamu.core.workers

import org.scalamu.testapi.AbstractTestSuite

final case class MeasuredSuite(suite: AbstractTestSuite, completionTimeMillis: Long)
