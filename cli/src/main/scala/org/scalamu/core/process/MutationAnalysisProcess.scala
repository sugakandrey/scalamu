package org.scalamu.core
package process

import java.io.{DataInputStream, DataOutputStream}

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.common.MutantId
import org.scalamu.utils.InMemoryClassLoader
import org.scalamu.utils.ClassLoadingUtils._

object MutationAnalysisProcess extends Process[MutationProcessResponse] {
  override type Configuration = (MutationAnalysisProcessConfig, Map[String, Array[Byte]])

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    for {
      config  <- decode[MutationAnalysisProcessConfig](dis.readUTF()).right
      classes <- Either.catchNonFatal(readCompiledSources(dis)).right
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
    val (config, classes) = configuration
    val classLoader       = new InMemoryClassLoader(classes)
    val id                = System.getProperty("worker.name")
    LoggerConfiguration.configureLoggingForName(s"MUTATION-WORKER-$id")
    MemoryWatcher.startMemoryWatcher(90)
    val runner = new SuiteRunner(config)

    withContextClassLoader(classLoader) {
      Iterator
        .continually(communicate(runner, dis, dos))
        .takeWhile(_.isRight)
        .map(_.right.get)
    }
  }

  def main(args: Array[String]): Unit = execute(args)
}
