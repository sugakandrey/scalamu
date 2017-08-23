package org.scalamu.core.runners

import java.io.IOException
import java.net.ServerSocket
import java.nio.file.Path
import java.util.concurrent._

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.process.{MeasuredSuite, MutationProcessResponse}
import org.scalamu.core._
import org.scalamu.plugin.MutantInfo

import scala.collection.JavaConverters._
import scala.collection.breakOut

class MutationAnalyser(
  val config: ScalamuConfig,
  val compiledSourcesDir: Path
) {
  import MutationAnalyser._

  private type Task   = (MutantId, Set[MeasuredSuite])
  private type Result = MutationAnalysisRunner#Result

  private val pool        = Executors.newFixedThreadPool(config.parallelism)
  private val jobQueue    = new ConcurrentLinkedQueue[Task]()
  private val resultQueue = new ConcurrentLinkedQueue[Result]()

  private class ProcessCommunicationWorker(
    socket: ServerSocket,
    processId: Long
  ) extends Runnable {
    private def initialiseProcessSupervisor()
      : Either[CommunicationException, ProcessSupervisor[Task, Result]] = {
      log.debug(s"Creating MutationAnalysisRunner#$processId ...")
      val runner        = new MutationAnalysisRunner(socket, config, compiledSourcesDir, processId)
      val tryInitialise = runner.start()
      tryInitialise.left.foreach(
        failure => s"Failed to start mutation analysis process. Cause: ${failure.cause}"
      )
      tryInitialise
    }

    private def drainJobQueue(supervisor: ProcessSupervisor[Task, Result]): Unit =
      Option(jobQueue.poll()).foreach { task =>
        val pipe = supervisor.communicationPipe
        Either.catchNonFatal(pipe.send(task))

        val response = pipe.receive()

        val (result, shouldRestart) = response match {
          case Right(res) => res -> false
          case Left(failure) =>
            log.debug(s"Task $task failed with $failure.")

            val (status, restart) = failure.cause match {
              case _: IOException if supervisor.waitFor(3, TimeUnit.SECONDS) =>
                val exitValue = supervisor.exitValue()
                RemoteProcessFailure.fromExitValue(exitValue) -> true
              case _ => InternalFailure -> false
            }

            MutationProcessResponse(task._1, status) -> restart
        }

        resultQueue.add(result)

        if (shouldRestart) {
          log.debug(s"Process#$processId was shut down. Restarting ...")
          if (!jobQueue.isEmpty) {
            initialiseProcessSupervisor().foreach(drainJobQueue)
          }
        } else drainJobQueue(supervisor)
      }

    override def run(): Unit = initialiseProcessSupervisor().foreach(drainJobQueue)
  }

  private def timeLimitInSeconds(coverage: Map[MutantId, Set[MeasuredSuite]]): Long = {
    val testTimeLimiter: (Long) => Long =
      process.testTimeLimit(_, config.timeoutFactor, config.timeoutConst)

    val totalTestsDurationMillis =
      coverage.values.map(_.map(t => testTimeLimiter(t.completionTimeMillis)).sum).sum

    val approximateRestarts    = Math.ceil(coverage.size * 1d / 50)
    val runnerCreationOverhead = 20
    val runnerOverhead         = approximateRestarts * runnerCreationOverhead
    (runnerOverhead + Math.ceil(totalTestsDurationMillis * 1d / 1000)).toLong
  }

  def analyse(
    coverage: Map[MutantId, Set[MeasuredSuite]],
    mutationsById: Map[MutantId, MutantInfo]
  ): Set[TestedMutant] = {
    jobQueue.addAll(coverage.toSeq.asJavaCollection)
    (1 to config.parallelism).foreach { id =>
      val socket   = new ServerSocket(0)
      val runnable = new ProcessCommunicationWorker(socket, id)
      pool.submit(runnable)
    }
    pool.shutdown()

    val timeLimit  = timeLimitInSeconds(coverage)
    val terminated = pool.awaitTermination(timeLimit, TimeUnit.SECONDS)

    val completed: Set[TestedMutant] =
      resultQueue.asScala.map(r => TestedMutant(mutationsById(r.id), r.status))(breakOut)

    if (terminated) completed
    else {
      log.debug(
        s"Some mutation analysis workers have timed out (time limit $timeLimit)." +
          s" Make sure timeoutFactor & timeoutConst are set correctly."
      )
      pool.shutdownNow()

      val unfinished: Set[TestedMutant] =
        jobQueue.asScala.map(task => TestedMutant(mutationsById(task._1), Untested))(breakOut)

      completed | unfinished
    }
  }
}

object MutationAnalyser {
  private val log = Logger[MutationAnalyser]
}
