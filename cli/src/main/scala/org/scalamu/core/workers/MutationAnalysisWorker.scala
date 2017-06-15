package org.scalamu.core
package workers

import java.io.DataInputStream
import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.common.MutantId
import org.scalamu.core.runners._

object MutationAnalysisWorker extends Worker[MutationWorkerResponse] {
  private val log = Logger[MutationAnalysisWorker.type]

  override type Configuration = (MutationAnalysisWorkerConfig, Map[MutantId, Set[MeasuredSuite]])

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] = {
    var totalRead = 0
    val length    = dis.readInt()
    val data      = Array.ofDim[Byte](length)
    while (totalRead < length) {
      val bytesRead = dis.read(data, totalRead, length - totalRead)
      totalRead += bytesRead
    }

    val parseCoverage =
      decode[Map[MutantId, Set[MeasuredSuite]]](new String(data, StandardCharsets.UTF_8))

    val parseConfig = decode[MutationAnalysisWorkerConfig](dis.readUTF())

    for {
      coverage <- parseCoverage
      config   <- parseConfig
    } yield (config, coverage)
  }

  override def run(
    configuration: Configuration
  ): Iterator[MutationWorkerResponse] = {
    MemoryWatcher.startMemoryWatcher(90)
    val (config, inverseCoverage) = configuration
    val runner                    = new SuiteRunner(config)
    inverseCoverage.iterator.map(
      Function.tupled(runner.runMutantInverseCoverage)
    )
  }

  def main(args: Array[String]): Unit = execute(args)
}
