package org.scalamu.testapi

import org.scalamu.testapi.junit.JUnitFramework
import io.circe.{Decoder, Encoder, KeyEncoder}
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

trait TestingFramework {
  type R

  def arguments: String
  def name: String
  def runner: TestRunner[R]
  def classFilter: TestClassFilter
}

object TestingFramework {
  val allFrameworks: Seq[TestingFramework] = Seq(
    JUnitFramework,
    Specs2Framework,
    ScalaTestFramework,
    UTestFramework
  )

  val frameworkByName: Map[String, TestingFramework] =
    allFrameworks.map(framework => framework.name.toLowerCase -> framework)(collection.breakOut)

  implicit val encodeFramework: Encoder[TestingFramework] = Encoder.encodeString.contramap(_.name.toLowerCase)
  implicit val decodeFramework: Decoder[TestingFramework] = Decoder.decodeString.emap(
    name =>
      frameworkByName.get(name) match {
        case Some(framework) => Right(framework)
        case _               => Left(s"$name is not a supported framework.")
    }
  )
  
  implicit val keyEncoder: KeyEncoder[TestingFramework] = framework =>
    KeyEncoder.encodeKeyString(framework.name)
}
