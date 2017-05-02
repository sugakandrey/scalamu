package org.scalamu.core
package runners

import org.scalamu.core.DetectionStatus.{Killed, NotCovered}
import org.scalamu.core.RunnerFailure.RuntimeFailure
import org.scalamu.plugin.MutantInfo
import org.scalamu.testapi.{SuiteExecutionAborted, AbstractTestSuite, SuiteSuccess, TestsFailed}

import scala.annotation.tailrec

object MutationAnalysisSuiteRunner {
  def runMutantInverseCoverage(
    info: MutantInfo,
    suites: Set[AbstractTestSuite]
  ): MutationResult = MutationResult(info, runSuites(suites))

  def runMutantsInverseCoverage(
    inverseCov: Map[MutantInfo, Set[AbstractTestSuite]]
  ): Set[MutationResult] =
    inverseCov.map(Function.tupled(runMutantInverseCoverage))(collection.breakOut)

  private def runSuites(suites: Set[AbstractTestSuite]): DetectionStatus = {
    @tailrec
    def loop(suites: Iterator[AbstractTestSuite]): DetectionStatus =
      if (suites.hasNext) {
        suites.next().execute() match {
          case _: SuiteSuccess     => loop(suites)
          case _: SuiteExecutionAborted     => RuntimeFailure
          case _: TestsFailed => Killed
        }
      } else DetectionStatus.Alive

    if (suites.isEmpty) NotCovered
    else loop(suites.iterator)
  }
}
