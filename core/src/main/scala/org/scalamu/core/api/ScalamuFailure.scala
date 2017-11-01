package org.scalamu.core.api

import org.scalamu.core.testapi.SuiteFailure
import cats.data.{NonEmptyList => NEL}

sealed trait ScalamuFailure

final case class FailingSuites(tests: NEL[SuiteFailure])  extends ScalamuFailure
final case class CommunicationException(cause: Throwable) extends ScalamuFailure
