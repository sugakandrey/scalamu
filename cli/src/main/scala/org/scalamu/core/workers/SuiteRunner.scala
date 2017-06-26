package org.scalamu.core
package workers

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.core.compilation.MutationGuard
import org.scalamu.testapi.{SuiteExecutionAborted, SuiteSuccess, TestsFailed}

import scala.collection.{Map, Set}
import scala.annotation.tailrec
import scala.concurrent.{blocking, Await, Future, TimeoutException}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SuiteRunner(config: MutationAnalysisWorkerConfig) {
  private val log = Logger[SuiteRunner]

  def runMutantInverseCoverage(
    id: MutantId,
    suites: Set[MeasuredSuite]
  ): MutationWorkerResponse = MutationWorkerResponse(id, runSuites(suites, id))

  def runMutantsInverseCoverage(
    inverseCov: Map[MutantId, Set[MeasuredSuite]]
  ): Set[MutationWorkerResponse] =
    inverseCov.map(Function.tupled(runMutantInverseCoverage))(collection.breakOut)

  private def runSuites(suites: Set[MeasuredSuite], id: MutantId): DetectionStatus = {
    MutationGuard.enableForId(id)
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
              case _: SuiteSuccess          => loop(suites)
              case _: SuiteExecutionAborted => die(ExitCode.RuntimeFailure)
              case f: TestsFailed           => Killed(f.name)
            }
          case Left(err) =>
            err match {
              case _: TimeoutException =>
                log.debug(
                  s"Mutation #${id.id} timed out. Worker will now shutdown."
                )
                die(ExitCode.TimedOut)
              case _ => die(ExitCode.RuntimeFailure)
            }
        }
      } else Alive

    if (suites.isEmpty) NoTestCoverage
    else loop(suites.iterator)
  }
}
