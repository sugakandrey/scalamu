package org.scalamu.core
package runners

import org.scalamu.testapi.{AbstractTestSuite, SuiteExecutionAborted, SuiteSuccess, TestsFailed}

import scala.annotation.tailrec

object MutationAnalysisSuiteRunner {
  def runMutantInverseCoverage(
    id: MutantId,
    suites: Set[AbstractTestSuite]
  ): MutationRunnerResponse = MutationRunnerResponse(id, runSuites(suites))

  def runMutantsInverseCoverage(
    inverseCov: Map[MutantId, Set[AbstractTestSuite]]
  ): Set[MutationRunnerResponse] =
    inverseCov.map(Function.tupled(runMutantInverseCoverage))(collection.breakOut)

  private def runSuites(suites: Set[AbstractTestSuite]): DetectionStatus = {
    @tailrec
    def loop(suites: Iterator[AbstractTestSuite]): DetectionStatus =
      if (suites.hasNext) {
        suites.next().execute() match {
          case _: SuiteSuccess          => loop(suites)
          case _: SuiteExecutionAborted => RuntimeFailure
          case _: TestsFailed           => Killed
        }
      } else Alive

    if (suites.isEmpty) NoTestCoverage
    else loop(suites.iterator)
  }
}
