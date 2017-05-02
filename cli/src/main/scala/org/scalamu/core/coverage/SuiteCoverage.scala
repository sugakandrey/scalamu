package org.scalamu.core.coverage

import org.scalamu.testapi.AbstractTestSuite
import scala.collection.Set

final case class SuiteCoverage(suite: AbstractTestSuite, coverage: Set[Statement])
