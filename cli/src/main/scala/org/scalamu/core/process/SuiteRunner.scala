package org.scalamu.core
package process

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.compilation.MutationGuard
import org.scalamu.testapi.{SuiteExecutionAborted, SuiteSuccess, TestsFailed}

import scala.collection.{Map, Set}
import scala.annotation.tailrec
import scala.concurrent.{Await, Future, TimeoutException, blocking}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SuiteRunner(config: MutationAnalysisProcessConfig) {
  private val log = Logger[SuiteRunner]

  def runMutantInverseCoverage(
    id: MutantId,
    suites: Set[MeasuredSuite]
  ): MutationProcessResponse = MutationProcessResponse(id, runSuites(suites, id))

  def runMutantsInverseCoverage(
    inverseCov: Map[MutantId, Set[MeasuredSuite]]
  ): Set[MutationProcessResponse] =
    inverseCov.map(Function.tupled(runMutantInverseCoverage))(collection.breakOut)

  private def runSuites(suites: Set[MeasuredSuite], id: MutantId): DetectionStatus = {
    MutationGuard.enableForId(id.id)
    @tailrec
    def loop(suites: Iterator[MeasuredSuite]): DetectionStatus =
      if (suites.hasNext) {
        val MeasuredSuite(suite, completionTime) = suites.next()
        log.debug(s"Executing suite ${suite.info.name.fullName} with mutant #${id.id}.")
        val testResult      = Future { blocking { suite.execute() } }
        val timeLimit       = testTimeLimit(completionTime, config.timeoutFactor, config.timeoutConst)
        val executionResult = Either.catchNonFatal(Await.result(testResult, timeLimit millis))

        executionResult match {
          case Right(result) =>
            result match {
              case _: SuiteSuccess => loop(suites)
              case f: TestsFailed  => Killed(f.name)
              case e: SuiteExecutionAborted =>
                log.debug(
                  s"Something went wrong when trying to run a test suite named " +
                    s"${suite.info.name.fullName}. Cause: $e."
                )
                die(InternalFailure)
            }
          case Left(err) =>
            err match {
              case _: TimeoutException =>
                log.debug(s"Mutation #${id.id} timed out. Worker will now shutdown.")
                die(TimedOut)
              case e =>
                log.debug(
                  s"An exception occurred, while waiting for test " +
                    s"${suite.info.name.fullName} to finish. Cause: $e."
                )
                die(InternalFailure)
            }
        }
      } else Alive

    if (suites.isEmpty) NoTestCoverage
    else loop(suites.iterator)
  }
}
