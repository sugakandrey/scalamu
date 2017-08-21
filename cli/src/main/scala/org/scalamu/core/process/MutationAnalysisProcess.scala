package org.scalamu.core
package process

import java.io.{DataInputStream, DataOutputStream}

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.common.MutantId

object MutationAnalysisProcess extends Process[MutationProcessResponse] {
  override type Configuration = (MutationAnalysisProcessConfig, Map[String, Array[Byte]])

  protected def readCompiledSources(dis: DataInputStream): Map[String, Array[Byte]] =
    Iterator.continually {
      val name   = dis.readUTF()
      val length = dis.readInt()
      dis.readUTF()
      val bytes = Array.ofDim[Byte](length)
      dis.readFully(bytes)
      name -> bytes
    }.toMap

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    for {
      config  <- decode[MutationAnalysisProcessConfig](dis.readUTF())
      classes <- readCompiledSources(dis)
    } yield (config, classes)

  private def communicate(
    runner: SuiteRunner,
    dis: DataInputStream,
    dos: DataOutputStream
  ): Either[Throwable, MutationProcessResponse] = Either.catchNonFatal {
    val data         = dis.readUTF()
    val mutationData = decode[(MutantId, Set[MeasuredSuite])](data)

    mutationData match {
      case Left(parsingError) =>
        Console.err.println(s"Error parsing data from runner. $parsingError")
        die(InternalFailure)
      case Right((id, suites)) => runner.runMutantInverseCoverage(id, suites)
    }
  }

  override def run(
    configuration: Configuration,
    dis: DataInputStream,
    dos: DataOutputStream
  ): Iterator[MutationProcessResponse] = {
    val id = System.getProperty("worker.name")
    LoggerConfiguration.configureLoggingForName(s"MUTATION-WORKER-$id")
    MemoryWatcher.startMemoryWatcher(90)
    val runner = new SuiteRunner(configuration)

    Iterator
      .continually(communicate(runner, dis, dos))
      .takeWhile(_.isRight)
      .map(_.right.get)
  }

  def main(args: Array[String]): Unit = execute(args)
}
