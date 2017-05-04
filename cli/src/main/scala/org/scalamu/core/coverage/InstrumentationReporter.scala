package org.scalamu.core.coverage

import com.typesafe.scalalogging.Logger
import scoverage.Coverage

trait InstrumentationReporter {
  def onInstrumentationFinished(coverage: Coverage): Unit
  def getStatementById(id: Int): Statement
}

object InstrumentationReporter {
  import io.circe.syntax._
  import io.circe.{Decoder, Encoder}

  private val log = Logger[InstrumentationReporter]

  implicit val encodeReporter: Encoder[InstrumentationReporter] = {
    case reporter: MemoryReporter => reporter.asJson
    case other                    => throw new IllegalArgumentException(s"$other is not supposed to be serialized.")
  }

  implicit val decoderReporter: Decoder[InstrumentationReporter] = _.as[MemoryReporter]
}
