package org.scalatest.tools

import org.scalamu.testapi.scalatest.ScalaTestArgs

import cats.syntax.either._

object ScalaTestInteractionLayer {
  def parseArgumentsString(argString: String): Either[Throwable, ScalaTestArgs] = {
    val args             = argString.split("\\s+")
    val isValidArgString = Either.catchNonFatal(ArgsParser.checkArgsForValidity(args))

    isValidArgString.flatMap {
      case Some(errorMessage) =>
        Left(new IllegalArgumentException(s"Invalid ScalaTest argument string: $errorMessage."))
      case None =>
        val ParsedArgs(
          _,
          _,
          _,
          _,
          _,
          properties,
          tagsToIncludeArgs,
          tagsToExcludeArgs,
          _,
          _,
          _,
          _,
          _,
          _,
          spanScaleFactorArgs,
          _,
          _
        ) = ArgsParser.parseArgs(args)

        val parsePropertiesMap =
          Either.catchNonFatal(ArgsParser.parsePropertiesArgsIntoMap(properties))

        val parseTagsToInclude =
          Either.catchNonFatal(ArgsParser.parseCompoundArgIntoSet(tagsToIncludeArgs, "-n"))

        val parseTagsToExclude =
          Either.catchNonFatal(ArgsParser.parseCompoundArgIntoSet(tagsToExcludeArgs, "-l"))

        val parseSpanScaleFactor =
          Either.catchNonFatal(ArgsParser.parseDoubleArgument(spanScaleFactorArgs, "-F", 1.0))

        for {
          properties      <- parsePropertiesMap
          include         <- parseTagsToInclude
          exclude         <- parseTagsToExclude
          spanScaleFactor <- parseSpanScaleFactor
        } yield ScalaTestArgs(properties, include, exclude, spanScaleFactor)
    }
  }

  def setSpanScaleFactor(spanScaleFactor: Double): Unit = Runner.spanScaleFactor = spanScaleFactor
}
