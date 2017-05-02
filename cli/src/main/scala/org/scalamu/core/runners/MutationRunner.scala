package org.scalamu.core
package runners

import org.scalamu.plugin.MutantInfo
import org.scalamu.testapi.AbstractTestSuite

object MutationRunner {
  def main(args: Array[String]): Unit = run(???)

  type ProcessData = (String, Set[MutationResult])

  def run(
    inverseCoverage: Map[MutantInfo, Set[AbstractTestSuite]]
  ): Map[String, Set[MutationResult]] = {
    val results = MutationAnalysisSuiteRunner.runMutantsInverseCoverage(inverseCoverage)
    results.groupBy(_.info.pos.source)
  }
}
