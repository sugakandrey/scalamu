package org.scalamu.core
package runners

import java.io.DataInputStream

import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.testapi.AbstractTestSuite

object MutationRunner extends Runner[MutationRunnerResponse] {
  private val log = Logger[MutationRunner.type]

  override type Configuration = Map[MutantId, Set[AbstractTestSuite]]

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Map[MutantId, Set[AbstractTestSuite]]] =
    decode[Configuration](dis.readUTF())

  override def run(
    inverseCoverage: Configuration
  ): Iterator[MutationRunnerResponse] =
    inverseCoverage.iterator.map(
      Function.tupled(MutationAnalysisSuiteRunner.runMutantInverseCoverage)
    )

  def main(args: Array[String]): Unit = {
    execute(args)
  }
}
