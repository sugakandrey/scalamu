package org.scalamu.core.runners

import java.net.ServerSocket
import java.nio.file.Path
import java.util.concurrent._

import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutantId
import org.scalamu.core.{TestedMutant, WorkerFailure, workers}
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.workers.MeasuredSuite
import org.scalamu.plugin.MutantInfo

import scala.collection.JavaConverters._

class MutationAnalyser2(
  val config: ScalamuConfig,
  val compiledSourcesDir: Path
) {
  import MutationAnalyser2._
  private type Task = (MutantId, Set[MeasuredSuite])

  private val pool        = Executors.newFixedThreadPool(config.parallelism)
  private val jobQueue    = new ConcurrentLinkedQueue[Task]()
  private val resultQueue = new ConcurrentLinkedQueue[MutationAnalysisRunner#Result]()

  private class RunnerHandler(
    socket: ServerSocket,
    workerId: Long
  ) extends Runnable {
    private def initializeRunner(): MutationAnalysisRunner = {
      val runner = new MutationAnalysisRunner(socket, config, compiledSourcesDir, workerId)
      runner.start()
      runner
    }

    private def drainJobQueue(runner: CommunicationPipe[Task, Nothing]): Unit =
      Option(jobQueue.poll()).foreach { task =>
        val response = runner.exchangeData(task).fold()
        resultQueue.add(response)
        val newRunner = response match {
          case failure: WorkerFailure =>
            log.debug(s"Worker#$workerId was shut down. Restarting ...")
            initializeRunner()
          case _ => runner
        }
        drainJobQueue(newRunner)
      }

    override def run(): Unit = drainJobQueue(initializeRunner())
  }
  
  private def timeLimitInSeconds(coverage: Map[MutantId, Set[MeasuredSuite]]): Long = {
    val testTimeLimiter: (Long) => Long =
      workers.testTimeLimit(_, config.timeoutFactor, config.timeoutConst)

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
    (1 to config.parallelism).foreach { id =>
      val socket   = new ServerSocket(0)
      val runnable = new RunnerHandler(socket, id)
      pool.submit(runnable)
    }
    pool.shutdown()
    pool.awaitTermination(timeLimitInSeconds(coverage), TimeUnit.SECONDS)
    resultQueue.asScala.map(r => TestedMutant(mutationsById(r.id), r.status))(collection.breakOut)
  }
}

object MutationAnalyser2 {
  private val log = Logger[MutationAnalyser2]
}
