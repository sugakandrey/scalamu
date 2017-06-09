package org.scalamu.core
package workers

import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.core.compilation.MutationGuard
import org.scalamu.testapi.{AbstractTestSuite, SuiteExecutionAborted, SuiteSuccess, TestsFailed}

import scala.collection.{Map, Set}
import scala.annotation.tailrec

object SuiteRunner {
  private val log = Logger[SuiteRunner.type]

  def runMutantInverseCoverage(
    id: MutantId,
    suites: Set[AbstractTestSuite]
  ): MutationWorkerResponse = MutationWorkerResponse(id, runSuites(suites, id))

  def runMutantsInverseCoverage(
    inverseCov: Map[MutantId, Set[AbstractTestSuite]]
  ): Set[MutationWorkerResponse] =
    inverseCov.map(Function.tupled(runMutantInverseCoverage))(collection.breakOut)

  private def runSuites(suites: Set[AbstractTestSuite], id: MutantId): DetectionStatus = {
    MutationGuard.enableForId(id)
    @tailrec
    def loop(suites: Iterator[AbstractTestSuite]): DetectionStatus =
      if (suites.hasNext) {
        val suite = suites.next()
        log.debug(s"Executing suite ${suite.info.name.fullName} with mutant #${id.id}.")
        suite.execute() match {
          case _: SuiteSuccess          => loop(suites)
          case _: SuiteExecutionAborted => RuntimeFailure
          case f: TestsFailed           => Killed(f.name)
        }
      } else Alive

    if (suites.isEmpty) NoTestCoverage
    else loop(suites.iterator)
  }
}
