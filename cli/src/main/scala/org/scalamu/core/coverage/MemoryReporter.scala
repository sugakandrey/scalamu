package org.scalamu.core.coverage

import java.{util => ju}

import org.scalamu.common.position.Position
import scoverage.Coverage

class MemoryReporter(
  protected val instrumentedStatements: ju.Map[Int, Statement] = new ju.HashMap[Int, Statement]
) extends InstrumentationReporter {
  def onInstrumentationFinished(coverage: Coverage): Unit =
    coverage.statements.foreach(
      stm =>
        instrumentedStatements.put(
          stm.id,
          Statement(
            stm.id,
            stm.line,
            Position(stm.source, stm.start, stm.end)
          )
      )
    )

  override def getStatementById(id: Int): Statement = instrumentedStatements.get(id)
}

object MemoryReporter {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.{Decoder, Encoder}
  import scala.collection.JavaConverters._

  implicit val encodeMemoryReporter: Encoder[MemoryReporter] =
    _.instrumentedStatements.asScala.toMap.asJson

  implicit val decodeMemoryReporter: Decoder[MemoryReporter] =
    _.value.as[Map[Int, Statement]].map(data => new MemoryReporter(data.asJava))
}
