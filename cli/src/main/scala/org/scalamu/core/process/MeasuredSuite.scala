package org.scalamu.core.process

import org.scalamu.testapi.AbstractTestSuite

final case class MeasuredSuite(suite: AbstractTestSuite, completionTimeMillis: Long)