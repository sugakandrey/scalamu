package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.process.{MutationAnalysisProcess, _}

class MutationAnalysisRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path,
  val workerId: Long,
  compiledSources: Map[String, Array[Byte]]
) extends Runner[(MutantId, Set[MeasuredSuite]), MutationAnalysisProcess.Result] {

  override protected def worker: Process[Result] = MutationAnalysisProcess

  private def writeCompiledSources(os: DataOutputStream): Unit = {
    os.writeInt(compiledSources.size)
    compiledSources.foreach {
      case (bname, bytes) =>
        os.writeUTF(bname)
        os.writeInt(bytes.length)
        os.write(bytes)
    }
  }

  override protected def sendConfigurationToWorker(os: DataOutputStream): Unit = {
    val configData = config.derive[MutationAnalysisProcessConfig].asJson.noSpaces
    os.writeUTF(configData)
    writeCompiledSources(os)
    os.flush()
  }

  override protected def generateProcessArgs(
    worker: Process[_],
    jvmArgs: String,
    runnerArgs: Seq[String]
  ): Seq[String] =
    super.generateProcessArgs(
      worker,
      s"-Dworker.name=$workerId $jvmArgs",
      runnerArgs
    )
}
