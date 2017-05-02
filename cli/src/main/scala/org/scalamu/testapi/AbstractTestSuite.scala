package org.scalamu.testapi

import org.scalamu.core.ClassInfo

trait AbstractTestSuite {
  def info: ClassInfo
  def execute(): TestSuiteResult
}

object AbstractTestSuite {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.{Decoder, Encoder}

  implicit val encodeTestSuite: Encoder[AbstractTestSuite] = {
    case info: TestClassInfo => info.asJson
    case other               => throw new IllegalArgumentException(s"$other is not supposed to be serialized.")
  }

  implicit val decodeTestSuite: Decoder[AbstractTestSuite] = _.value.as[TestClassInfo]
}
