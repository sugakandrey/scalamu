package org.scalamu.core
package process

import cats.syntax.either._
import org.scalamu.core.api._
import org.scalamu.common.MutationId
import org.scalamu.compilation.MutationGuard
import org.scalamu.core.testapi.{SuiteExecutionAborted, SuiteSuccess, TestsFailed}

import scala.collection.{Map, Set}
import scala.annotation.tailrec
import scala.concurrent.{blocking, Await, Future, TimeoutException}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SuiteRunner(config: MutationAnalysisProcessConfig) {
  def runMutantInverseCoverage(
    id: MutationId,
    suites: Set[MeasuredSuite]
  ): MutationAnalysisProcessResponse = MutationAnalysisProcessResponse(id, runSuites(suites, id))

  def runMutantsInverseCoverage(
    inverseCov: Map[MutationId, Set[MeasuredSuite]]
  ): Set[MutationAnalysisProcessResponse] =
    inverseCov.map(Function.tupled(runMutantInverseCoverage))(collection.breakOut)

  private def runSuites(suites: Set[MeasuredSuite], id: MutationId): DetectionStatus = {
    MutationGuard.enableForId(id.id)
    @tailrec
    def loop(suites: Iterator[MeasuredSuite]): DetectionStatus =
      if (suites.hasNext) {
        val MeasuredSuite(suite, completionTime) = suites.next()
        val suiteName = suite.info.name.fullName
        scribe.debug(s"Executing suite $suiteName with mutant #${id.id}.")
        val testResult      = Future { blocking { suite.execute() } }
        val timeLimit       = testTimeLimit(completionTime, config.timeoutFactor, config.timeoutConst)
        val executionResult = Either.catchNonFatal(Await.result(testResult, timeLimit millis))

        executionResult match {
          case Right(result) =>
            result match {
              case _: SuiteSuccess => loop(suites)
              case f: TestsFailed  => 
                scribe.debug(s"Mutation #${id.id} killed by suite $suiteName")
                Killed(f.name)
              case e: SuiteExecutionAborted =>
                scribe.debug(
                  s"Something went wrong when trying to run " +
                    s"$suiteName. Cause: $e."
                )
                die(InternalFailure)
            }
          case Left(err) =>
            err match {
              case _: TimeoutException =>
                scribe.debug(s"Mutation #${id.id} timed out. Worker will now shutdown.")
                die(TimedOut)
              case e =>
                scribe.debug(
                  s"An exception occurred, while waiting for test " +
                    s"$suiteName to finish. Cause: $e."
                )
                die(InternalFailure)
            }
        }
      } else Alive

    if (suites.isEmpty) NoTestCoverage
    else loop(suites.iterator)
  }
}
