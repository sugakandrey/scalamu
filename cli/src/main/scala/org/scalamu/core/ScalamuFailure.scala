package org.scalamu.core

import org.scalamu.testapi.SuiteFailure
import cats.data.{NonEmptyList => NEL}

sealed trait ScalamuFailure

final case class FailingSuites(tests: NEL[SuiteFailure])      extends ScalamuFailure
final case class CommunicationException(cause: Throwable) extends ScalamuFailure
