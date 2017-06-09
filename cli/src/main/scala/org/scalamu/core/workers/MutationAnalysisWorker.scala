package org.scalamu.core
package workers

import java.io.DataInputStream
import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.common.MutantId
import org.scalamu.core.runners._
import org.scalamu.testapi.AbstractTestSuite

object MutationAnalysisWorker extends Worker[MutationWorkerResponse] {
  private val log = Logger[MutationAnalysisWorker.type]

  override type Configuration = Map[MutantId, Set[AbstractTestSuite]]

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Map[MutantId, Set[AbstractTestSuite]]] = {
    val length = dis.readInt()
    val data   = Array.ofDim[Byte](length)
    dis.read(data)
    decode[Configuration](new String(data, StandardCharsets.UTF_8))
  }

  override def run(
    inverseCoverage: Configuration
  ): Iterator[MutationWorkerResponse] =
    inverseCoverage.iterator.map(
      Function.tupled(SuiteRunner.runMutantInverseCoverage)
    )

  def main(args: Array[String]): Unit = execute(args)
}
