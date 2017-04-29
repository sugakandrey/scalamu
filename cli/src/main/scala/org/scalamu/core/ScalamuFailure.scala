package org.scalamu.core

import org.scalamu.testapi.SuiteFailure
import cats.data.{NonEmptyList => NEL}

sealed trait ScalamuFailure

case object MalformedConfig                              extends ScalamuFailure
final case class FailingSuites(tests: NEL[SuiteFailure]) extends ScalamuFailure
