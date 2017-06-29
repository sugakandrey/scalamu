package org.scalamu.core
package process

import java.io.{DataInputStream, DataOutputStream}

import cats.syntax.either._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalamu.common.MutantId

object MutationAnalysisProcess extends Process[MutationProcessResponse] {
  override type Configuration = MutationAnalysisProcessConfig

  override def readConfigurationFromParent(
    dis: DataInputStream
  ): Either[Throwable, Configuration] =
    decode[MutationAnalysisProcessConfig](dis.readUTF())
  
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
      case Right((id, suites)) =>
        val shutdownHook = (status: RemoteProcessFailure) => {
          val failureResponse = MutationProcessResponse(id, status).asJson.noSpaces
          dos.writeUTF(failureResponse)
          dos.flush()
        }
        
        val result = runner.runMutantInverseCoverage(id, suites)
        
        result.status match {
          case f: RemoteProcessFailure => 
            shutdownHook(f)
            die(f)
          case _ => result
        }
    }
  }

  override def run(
    configuration: Configuration,
    dis: DataInputStream,
    dos: DataOutputStream
  ): Iterator[MutationProcessResponse] = {
    val id = System.getProperty("worker.name")
    LoggerConfiguration.configurePatternForName(s"MUTATION-WORKER-$id")
    MemoryWatcher.startMemoryWatcher(90)
    val runner = new SuiteRunner(configuration)

    Iterator
      .continually(communicate(runner, dis, dos))
      .takeWhile(_.isRight)
      .map(_.right.get)
  }

  def main(args: Array[String]): Unit = execute(args)
}
