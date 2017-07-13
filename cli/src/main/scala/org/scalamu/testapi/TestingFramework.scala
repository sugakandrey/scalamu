package org.scalamu.testapi

import org.scalamu.testapi.junit.JUnitFramework
import io.circe.{Decoder, Encoder}
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
  private val frameworkBuilderForName: Map[String, String => TestingFramework] = Map(
    "scalatest" -> ScalaTestFramework.apply,
    "junit"     -> JUnitFramework.apply,
    "utest"     -> UTestFramework.apply,
    "specs2"    -> Specs2Framework.apply
  )

  val frameworkNames: Set[String] = frameworkBuilderForName.keySet

  def instantiateAvailableFrameworks(
    frameworkOptions: Map[String, String] = Map.empty
  ): Seq[TestingFramework] = {

    def isClassResolvable(className: String): Boolean =
      try { Class.forName(className); true } catch { case _: ClassNotFoundException => false }

    val specs2 =
      if (isClassResolvable("org.specs2.specification.core.SpecificationStructure")) Some("specs2")
      else None
    val scalaTest = if (isClassResolvable("org.scalatest.Suite")) Some("scalatest") else None
    val utest     = if (isClassResolvable("utest.TestSuite")) Some("utest") else None
    val junit     = if (isClassResolvable("org.junit.Test")) Some("junit") else None
    Seq(specs2, scalaTest, utest, junit).flatten.map { framework =>
      val options = frameworkOptions.getOrElse(framework, "")
      TestingFramework.frameworkBuilderForName(framework)(options)
    }
  }

  implicit val encodeFramework: Encoder[TestingFramework] =
    Encoder.encodeTuple2[String, String].contramap(f => f.name.toLowerCase -> f.arguments)

  implicit val decodeFramework: Decoder[TestingFramework] =
    Decoder.decodeTuple2[String, String].emap {
      case (name, args) =>
        frameworkBuilderForName.get(name) match {
          case Some(frameworkBuilder) => Right(frameworkBuilder(args))
          case _                      => Left("s")
        }
    }
}
