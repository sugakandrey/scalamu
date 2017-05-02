package org.scalamu.testapi

import org.scalamu.testapi.junit.JUnitFramework
import io.circe.{Decoder, Encoder}
import org.scalamu.testapi.scalatest.ScalaTestFramework
import org.scalamu.testapi.specs2.Specs2Framework
import org.scalamu.testapi.utest.UTestFramework

trait TestingFramework {
  type R

  def name: String
  def runner: TestRunner[R]
  def filter: TestClassFilter
}

object TestingFramework {
  val frameworkByName: Map[String, TestingFramework] = Map(
    "JUnit"     -> JUnitFramework,
    "ScalaTest" -> ScalaTestFramework,
    "Specs2"    -> Specs2Framework,
    "utest"     -> UTestFramework
  )

  implicit val encodeFramework: Encoder[TestingFramework] = Encoder.encodeString.contramap(_.name)
  implicit val decodeFramework: Decoder[TestingFramework] = Decoder.decodeString.emap(
    name =>
      frameworkByName.get(name) match {
        case Some(framework) => Right(framework)
        case _               => Left(s"$name is not a supported framework.")
    }
  )
}
