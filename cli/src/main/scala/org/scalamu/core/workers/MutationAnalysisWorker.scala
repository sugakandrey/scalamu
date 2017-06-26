package org.scalamu.core
package workers

import java.io.DataInputStream

import cats.syntax.either._
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.common.MutantId
import org.scalamu.core.runners._

object MutationAnalysisWorker extends Worker[MutationWorkerResponse] {
  private val log = Logger[MutationAnalysisWorker.type]

  override type Configuration = MutationAnalysisWorkerConfig

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    decode[MutationAnalysisWorkerConfig](dis.readUTF())

  private def communicate(
    config: MutationAnalysisWorkerConfig,
    dis: DataInputStream
  ): Either[Throwable, MutationWorkerResponse] = Either.catchNonFatal {
    MemoryWatcher.startMemoryWatcher(90)
    val runner = new SuiteRunner(config)
    val data   = dis.readUTF()

    val mutationData = decode[(MutantId, Set[MeasuredSuite])](data)
    mutationData match {
      case Left(parsingError) =>
        Console.err.println(s"Error parsing data from runner. $parsingError")
        die(ExitCode.RuntimeFailure)
      case Right((id, suites)) => runner.runMutantInverseCoverage(id, suites)
    }
  }

  override def run(
    configuration: Configuration,
    dis: DataInputStream
  ): Iterator[MutationWorkerResponse] = {
    val id = System.getProperty("worker.name")
    LoggerConfiguration.configurePatternForName(s"MUTATION-WORKER-$id")

    Iterator
      .continually(communicate(configuration, dis))
      .takeWhile(_.isRight)
      .map(_.right.get)
  }

  def main(args: Array[String]): Unit = execute(args)
}
